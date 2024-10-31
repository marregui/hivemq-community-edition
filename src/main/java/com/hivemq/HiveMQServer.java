/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq;

import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Message;
import com.hivemq.bootstrap.ioc.GuiceBootstrap;
import com.hivemq.common.shutdown.ShutdownHooks;
import com.hivemq.configuration.ConfigurationBootstrap;
import com.hivemq.configuration.HivemqId;
import com.hivemq.configuration.info.SystemInformation;
import com.hivemq.configuration.info.SystemInformationImpl;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.exceptions.StartAbortedException;
import com.hivemq.exceptions.UnrecoverableException;
import com.hivemq.logging.modifier.XodusEnvironmentImplLogLevelModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.lifecycle.LifecycleModule;
import com.hivemq.metrics.MetricRegistryLogger;
import com.hivemq.persistence.PersistenceStartup;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;


public class HiveMQServer {

    private static final Logger log = LoggerFactory.getLogger(HiveMQServer.class);

    private final @NotNull HivemqId hivemqId = new HivemqId();
    private final @NotNull LifecycleModule lifecycleModule = new LifecycleModule();
    private final @NotNull DataFolderLock dataFolderLock = new DataFolderLock();
    private final @NotNull SystemInformation systemInformation = new SystemInformationImpl(true);
    private final @NotNull MetricRegistry metricRegistry = new MetricRegistry();

    private @Nullable Injector injector;
    private @Nullable FullConfigurationService configService;

    public static void main(final String @NotNull [] args) throws Exception {
        new HiveMQServer().start();
    }

    @VisibleForTesting
    static void handleUncaughtException(final Thread t, final Throwable e) {
        exitIfUnrecoverable(e);
        if (e instanceof CreationException) {
            exitIfUnrecoverable(e.getCause());
            exitIfUnrecoverable(((CreationException) e).getErrorMessages());
        } else if (e instanceof ProvisionException) {
            exitIfUnrecoverable(e.getCause());
            exitIfUnrecoverable(((ProvisionException) e).getErrorMessages());
        }
        log.error("Problem: %s%n", Throwables.getRootCause(e));
    }

    private static void exitIfUnrecoverable(final Collection<Message> errorMessages) {
        for (final Message message : errorMessages) {
            exitIfUnrecoverable(message.getCause());
        }
    }

    private static void exitIfUnrecoverable(final @NotNull Throwable t) {
        if (t instanceof UnrecoverableException) {
            log.error("An unrecoverable Exception occurred. Exiting HiveMQ: {}", t.getMessage());
            System.exit(1);
        }
    }

    public void start() throws Exception {
        final long startTime = System.nanoTime();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "shutdown-thread-" + hivemqId.get()));
        Thread.setDefaultUncaughtExceptionHandler(HiveMQServer::handleUncaughtException);

        // B O O T S T R A P
        systemInformation.init();
        metricRegistry.addListener(new MetricRegistryLogger());
        Logging.initLogging(systemInformation.getConfigFolder());
        log.info("Starting HiveMQ Community Edition Server");
        configService = ConfigurationBootstrap.bootstrapConfig(systemInformation);
        dataFolderLock.lock(systemInformation.getDataFolder().toPath());
        final File tmp = new File(systemInformation.getDataFolder().getPath() + File.separator + "tmp");
        try {
            FileUtils.deleteDirectory(tmp);
        } catch (final IOException e) {
            log.warn("The temporary folder could not be deleted ({}).", tmp);
        }
        final Injector persistence = GuiceBootstrap.persistenceInjector(systemInformation,
                metricRegistry,
                hivemqId,
                configService,
                lifecycleModule);
        persistence.getInstance(PersistenceStartup.class).finish();
        if (persistence.getInstance(ShutdownHooks.class).isShuttingDown()) {
            throw new StartAbortedException("User aborted.");
        }
        injector = GuiceBootstrap.bootstrapInjector(systemInformation,
                metricRegistry,
                hivemqId,
                configService,
                persistence,
                lifecycleModule);
        if (injector == null) {
            throw new UnrecoverableException(true);
        }

        // S T A R T    I N S T A N C E
        final HiveMQInstance instance = injector.getInstance(HiveMQInstance.class);
        final ShutdownHooks shutdownHooks = injector.getInstance(ShutdownHooks.class);
        System.gc();
        Logging.LOG_LEVEL_MODIFIER_TURBO_FILTER.registerLogLevelModifier(new XodusEnvironmentImplLogLevelModifier());
        instance.start();
        log.info("Started HiveMQ [{}] in {}ms",
                hivemqId.get(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
        if (shutdownHooks.isShuttingDown()) {
            throw new StartAbortedException("User aborted.");
        }
    }

    public void stop() {
        if (injector == null) {
            return;
        }
        final ShutdownHooks shutdownHooks = injector.getInstance(ShutdownHooks.class);
        if (shutdownHooks.isShuttingDown()) {
            return;
        }
        try {
            shutdownHooks.runShutdownHooks();
        } finally {
            dataFolderLock.unlock();
            Logging.resetLogging();
        }
    }
}
