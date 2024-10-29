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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;
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
import com.hivemq.logging.LogLevelModifierTurboFilter;
import com.hivemq.logging.modifier.NettyLogLevelModifier;
import com.hivemq.logging.modifier.XodusEnvironmentImplLogLevelModifier;
import com.hivemq.logging.modifier.XodusFileDataWriterLogLevelModifier;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.lifecycle.LifecycleModule;
import com.hivemq.metrics.MetricRegistryLogger;
import com.hivemq.persistence.PersistenceStartup;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    private static void exitIfUnrecoverable(final @com.hivemq.extension.sdk.api.annotations.NotNull Throwable t) {
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

    /**
     * This class is responsible for all logging bootstrapping. This is only
     * needed at the very beginning of HiveMQs lifecycle and before bootstrapping other
     * resources
     */
    public static class Logging {

        private static final @NotNull LoggerContext CONTEXT = (LoggerContext) LoggerFactory.getILoggerFactory();
        private static final @NotNull ch.qos.logback.classic.Logger ROOT_LOGGER =
                CONTEXT.getLogger(Logger.ROOT_LOGGER_NAME);
        private static final @NotNull ListAppender<ILoggingEvent> LIST_APPENDER = new ListAppender<>();
        private static final @NotNull List<Appender<ILoggingEvent>> DEFAULT_APPENDERS = new LinkedList<>();
        private static final @NotNull LogLevelModifierTurboFilter LOG_LEVEL_MODIFIER_TURBO_FILTER =
                new LogLevelModifierTurboFilter();
        private static final @NotNull LoggerContextListener LOGBACK_CHANGE_LISTENER = new LoggerContextListener() {

            @Override
            public boolean isResetResistant() {
                return true;
            }

            @Override
            public void onStart(final LoggerContext context) {
                //noop
            }

            @Override
            public void onReset(final LoggerContext context) {
                log.trace("logback.xml was changed");
                context.addTurboFilter(LOG_LEVEL_MODIFIER_TURBO_FILTER);
            }

            @Override
            public void onStop(final LoggerContext context) {
                //noop
            }

            @Override
            public void onLevelChange(final ch.qos.logback.classic.Logger logger, final Level level) {
                //noop
            }
        };

        public static void initLogging(final @NotNull File configFolder) {
            for (final Iterator<Appender<ILoggingEvent>> it = ROOT_LOGGER.iteratorForAppenders(); it.hasNext(); ) {
                final Appender<ILoggingEvent> appender = it.next();
                ROOT_LOGGER.detachAppender(appender);
                DEFAULT_APPENDERS.add(appender);
            }
            LIST_APPENDER.start();
            ROOT_LOGGER.addAppender(LIST_APPENDER);
            CONTEXT.addListener(LOGBACK_CHANGE_LISTENER);
            final boolean overridden = tryToOverrideLogbackXml(configFolder);
            if (!overridden) {
                for (final Appender<ILoggingEvent> defaultAppender : DEFAULT_APPENDERS) {
                    ROOT_LOGGER.addAppender(defaultAppender);
                }
                CONTEXT.addTurboFilter(LOG_LEVEL_MODIFIER_TURBO_FILTER);
                logQueuedEntries();
            }
            // redirect JUL to SLF4J
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            DEFAULT_APPENDERS.clear();
            LIST_APPENDER.list.clear();

            // must be added here, as addLoglevelModifiers() is much to late
            if (SystemUtils.IS_OS_WINDOWS) {
                LOG_LEVEL_MODIFIER_TURBO_FILTER.registerLogLevelModifier(new XodusFileDataWriterLogLevelModifier());
                log.trace("Added Xodus log level modifier for FileDataWriter.class");
            }
            LOG_LEVEL_MODIFIER_TURBO_FILTER.registerLogLevelModifier(new NettyLogLevelModifier());
            log.trace("Added Netty log level modifier");
        }

        public static void resetLogging() {
            CONTEXT.getTurboFilterList().remove(LOG_LEVEL_MODIFIER_TURBO_FILTER);
            CONTEXT.removeListener(LOGBACK_CHANGE_LISTENER);
        }


        private static void logQueuedEntries() {
            LIST_APPENDER.stop();
            ROOT_LOGGER.detachAppender(LIST_APPENDER);
            for (final ILoggingEvent loggingEvent : LIST_APPENDER.list) {
                ROOT_LOGGER.callAppenders(loggingEvent);
            }
            LIST_APPENDER.list.clear();
        }

        private static boolean tryToOverrideLogbackXml(final @NotNull File configFolder) {
            final File file = new File(configFolder, "logback.xml");
            if (file.canRead()) {
                try {
                    CONTEXT.reset();

                    final JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(CONTEXT);
                    configurator.doConfigure(file);
                    logQueuedEntries();
                    log.info("Log Configuration was overridden by {}", file.getAbsolutePath());
                    return true;
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    StatusPrinter.printInCaseOfErrorsOrWarnings(CONTEXT);
                }
                // Print internal status data in case of warnings or errors.
            } else { // we do not override if the custom config file does not exist
                log.warn(
                        "The logging configuration file {} cannot be read or does not exist. Using HiveMQ default logging configuration.",
                        file.getAbsolutePath());
            }
            return false;
        }
    }
}
