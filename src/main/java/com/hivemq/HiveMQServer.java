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
import com.google.common.base.Throwables;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.spi.Message;
import com.hivemq.bootstrap.HiveMQMainModule;
import com.hivemq.bootstrap.HiveMQNettyBootstrap;
import com.hivemq.bootstrap.ListenerStartupInformation;
import com.hivemq.bootstrap.SystemInformationModule;
import com.hivemq.bootstrap.lazysingleton.LazySingletonModule;
import com.hivemq.bootstrap.netty.NettyModule;
import com.hivemq.configuration.HivemqId;
import com.hivemq.configuration.info.SystemInformation;
import com.hivemq.configuration.ioc.ConfigurationFileProvider;
import com.hivemq.configuration.ioc.ConfigurationModule;
import com.hivemq.configuration.reader.ConfigFileReader;
import com.hivemq.configuration.reader.ListenerConfigurator;
import com.hivemq.configuration.reader.MqttConfigurator;
import com.hivemq.configuration.reader.RestrictionConfigurator;
import com.hivemq.configuration.reader.SecurityConfigurator;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.configuration.service.entity.Listener;
import com.hivemq.configuration.service.impl.ConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.MqttConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.RestrictionsConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.SecurityConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.listener.ListenerConfigurationServiceImpl;
import com.hivemq.extensions.ExtensionBootstrap;
import com.hivemq.extensions.ioc.ExtensionModule;
import com.hivemq.metrics.ioc.MetricsModule;
import com.hivemq.mqtt.ioc.MQTTHandlerModule;
import com.hivemq.mqtt.ioc.MQTTServiceModule;
import com.hivemq.persistence.ioc.PersistenceMigrationModule;
import com.hivemq.persistence.ioc.PersistenceModule;
import com.hivemq.persistence.payload.PublishPayloadPersistence;
import com.hivemq.security.ioc.SecurityModule;
import com.hivemq.util.Checkpoints;
import com.hivemq.util.EnvVarUtil;
import org.jetbrains.annotations.NotNull;
import com.hivemq.metrics.MetricRegistryLogger;
import com.hivemq.persistence.PersistenceStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public final class HiveMQServer {

    private static final Logger log = LoggerFactory.getLogger(HiveMQServer.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            exitIfUnrecoverable(e);
            final boolean atCreation = e instanceof CreationException;
            if (atCreation || e instanceof ProvisionException) {
                exitIfUnrecoverable(e.getCause());
                for (final Message message : atCreation ?
                        ((CreationException) e).getErrorMessages() :
                        ((ProvisionException) e).getErrorMessages()) {
                    exitIfUnrecoverable(message.getCause());
                }
            }
            log.error("Problem: %s%n", Throwables.getRootCause(e));
        });
        System.setProperty("guice_include_stack_traces", "OFF");
    }

    private static void exitIfUnrecoverable(final @NotNull Throwable t) {
        if (t instanceof UnrecoverableException) {
            System.err.printf("unrecoverable: %s%n", t.getMessage());
            System.exit(1);
        }
    }

    public static void main(final String @NotNull [] args) throws Exception {
        final SystemInformation sysInfo = new SystemInformation();
        Logging.initLogging(sysInfo.getConfigFolder());
        final HivemqId hivemqId = new HivemqId();
        final LifecycleModule lifecycleModule = new LifecycleModule();
        final DataFolderLock dataLock = new DataFolderLock();
        final MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.addListener(new MetricRegistryLogger());

        final FullConfigurationService config = new ConfigurationServiceImpl(new ListenerConfigurationServiceImpl(),
                new MqttConfigurationServiceImpl(),
                new RestrictionsConfigurationServiceImpl(),
                new SecurityConfigurationServiceImpl());
        final ConfigFileReader configReader = new ConfigFileReader(ConfigurationFileProvider.get(sysInfo),
                new RestrictionConfigurator(config.restrictionsConfiguration()),
                new SecurityConfigurator(config.securityConfiguration()),
                new EnvVarUtil(),
                new MqttConfigurator(config.mqttConfiguration()),
                new ListenerConfigurator(config.listenerConfiguration(), sysInfo));
        configReader.applyConfig();

        dataLock.lock(sysInfo.getDataFolder().toPath());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ShutdownHooks.INSTANCE.shutdown();
            } finally {
                dataLock.unlock();
                Logging.resetLogging();
            }
        }, "shutdown-" + hivemqId.get()));

        final Injector persistence = Guice.createInjector(Stage.PRODUCTION,
                Arrays.asList(new SystemInformationModule(sysInfo),
                        new ConfigurationModule(config, hivemqId),
                        new LazySingletonModule(),
                        lifecycleModule,
                        new PersistenceMigrationModule(metricRegistry)));
        persistence.getInstance(PersistenceStartup.class).finish();

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                Arrays.asList(new SystemInformationModule(sysInfo),
                        new LazySingletonModule(),
                        lifecycleModule,
                        new ConfigurationModule(config, hivemqId),
                        new NettyModule(),
                        new HiveMQMainModule(),
                        new MQTTHandlerModule(persistence),
                        new PersistenceModule(persistence),
                        new MetricsModule(metricRegistry, persistence),
                        new ThrottlingModule(),
                        new MQTTServiceModule(),
                        new SecurityModule(),
                        new ExtensionModule()));

        // start
        final long startTime = System.nanoTime();
        injector.getInstance(PublishPayloadPersistence.class).init();
        injector.getInstance(ExtensionBootstrap.class).startExtensionSystem().get();
        final List<ListenerStartupInformation> startupInformation =
                Objects.requireNonNull(injector.getInstance(HiveMQNettyBootstrap.class).bootstrapServer().get());
        Checkpoints.checkpoint("listener-started");
        if (startupInformation.isEmpty()) {
            log.error("No listener was configured");
            throw new UnrecoverableException();
        }
        int success = 0;
        for (final ListenerStartupInformation info : startupInformation) {
            if (info.isSuccessful()) {
                final Listener listener = info.getListener();
                log.info("Started {} on address {} and on port {}.",
                        listener.readableName(),
                        listener.getBindAddress(),
                        listener.getPort());
                success++;
            } else {
                final Listener listener = info.getListener();
                log.error("Could not start {} on port {} and address {}. Is it already in use?",
                        listener.readableName(),
                        listener.getPort(),
                        listener.getBindAddress());
            }
        }
        if (success < 1) {
            log.error("Could not bind any listener. Stopping HiveMQ.");
            throw new UnrecoverableException();
        }
        System.gc();
        log.info("Started HiveMQ [{}] in {}ms",
                hivemqId.get(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }
}
