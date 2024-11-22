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
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.hivemq.bootstrap.SingletonModule;
import com.google.inject.Singleton;
import com.hivemq.bootstrap.netty.ChannelInitializerFactory;
import com.hivemq.bootstrap.netty.ChannelInitializerFactoryImpl;
import com.hivemq.bootstrap.netty.NettyConfiguration;
import com.hivemq.bootstrap.netty.NettyConfigurationProvider;
import com.hivemq.config.SysInfo;
import com.hivemq.config.ConfigModule;
import com.hivemq.config.ConfigService;
import com.hivemq.config.InternalConfig;
import com.hivemq.config.RestrictionsConfigService;
import com.hivemq.extensions.ioc.ExtensionModule;
import com.hivemq.metrics.MetricRegistryLogger;
import com.hivemq.metrics.MetricsHolder;
import com.hivemq.metrics.MetricsShutdownHook;
import com.hivemq.metrics.gauges.OpenConnectionsGauge;
import com.hivemq.metrics.gauges.RetainedMessagesGauge;
import com.hivemq.metrics.gauges.SessionsGauge;
import com.hivemq.metrics.ioc.provider.OpenConnectionsGaugeProvider;
import com.hivemq.metrics.ioc.provider.RetainedMessagesGaugeProvider;
import com.hivemq.metrics.ioc.provider.SessionsGaugeProvider;
import com.hivemq.metrics.jmx.JmxReporterBootstrap;
import com.hivemq.mqtt.handler.connack.MqttConnacker;
import com.hivemq.mqtt.handler.connack.MqttConnackerImpl;
import com.hivemq.mqtt.handler.disconnect.MqttServerDisconnector;
import com.hivemq.mqtt.handler.disconnect.MqttServerDisconnectorImpl;
import com.hivemq.mqtt.message.dropping.MessageDroppedService;
import com.hivemq.mqtt.services.InternalPublishService;
import com.hivemq.mqtt.services.InternalPublishServiceImpl;
import com.hivemq.mqtt.services.PublishDistributor;
import com.hivemq.mqtt.services.PublishDistributorImpl;
import com.hivemq.mqtt.services.PublishPollService;
import com.hivemq.mqtt.services.PublishPollServiceImpl;
import com.hivemq.topics.TokenizedTopicMatcher;
import com.hivemq.topics.TopicMatcher;
import com.hivemq.topics.tree.TopicTreeStartup;
import com.hivemq.persistence.PersistenceShutdownHookInstaller;
import com.hivemq.persistence.PersistenceStartup;
import com.hivemq.persistence.ScheduledCleanUpService;
import com.hivemq.persistence.ioc.LocalPersistenceModule;
import com.hivemq.persistence.ioc.PersistenceMigrationModule;
import com.hivemq.persistence.ioc.annotation.PayloadPersistence;
import com.hivemq.persistence.ioc.annotation.Persistence;
import com.hivemq.persistence.ioc.provider.local.PayloadPersistenceScheduledExecutorProvider;
import com.hivemq.persistence.ioc.provider.local.PersistenceExecutorProvider;
import com.hivemq.persistence.ioc.provider.local.PersistenceScheduledExecutorProvider;
import com.hivemq.persistence.util.FutureUtils;
import com.hivemq.security.ioc.Security;
import com.hivemq.security.ioc.SecurityExecutorProvider;
import com.hivemq.security.ssl.SslContextStore;
import com.hivemq.security.ssl.SslFactory;
import com.hivemq.util.ThreadFactoryUtil;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.hivemq.config.InternalConfig.MQTT_EVENT_EXECUTOR_THREAD_COUNT;

public class IOC extends SingletonModule<IOC> {

    private final @NotNull MetricRegistry metricRegistry;
    private final @NotNull LifecycleModule lifecycle = new LifecycleModule();
    private final @NotNull ConfigModule configuration;
    private @Nullable Injector injector;

    public IOC(final @NotNull ConfigService config) throws InterruptedException {
        super(IOC.class);
        configuration = new ConfigModule(config);
        metricRegistry = new MetricRegistry();
        metricRegistry.addListener(new MetricRegistryLogger());
        // lock data folder
        final DataFolderLock dataLock = new DataFolderLock();
        dataLock.lock(SysInfo.INSTANCE.getDataFolder().toPath());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ShutdownHooks.INSTANCE.shutdown();
            } finally {
                dataLock.unlock();
                Logging.resetLogging();
            }
        }, "shutdown-" + configuration.getHiveMQId()));
        injector = Guice.createInjector(Stage.PRODUCTION,
                Arrays.asList(lifecycle, configuration, new PersistenceMigrationModule(metricRegistry)));
        injector.getInstance(PersistenceStartup.class).finish();
    }

    public @NotNull String getHiveMQId() {
        return configuration.getHiveMQId();
    }

    public @NotNull Injector init() {
        final Injector finalInjector = Guice.createInjector(Stage.PRODUCTION,
                Arrays.asList(this, lifecycle, configuration, new ExtensionModule()));
        injector = null;
        return finalInjector;
    }

    @Override
    protected void configure() {
        // netty
        bind(ChannelGroup.class).toInstance(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        bind(NettyConfiguration.class).toProvider(NettyConfigurationProvider.class).in(Singleton.class);
        bind(ChannelInitializerFactory.class).to(ChannelInitializerFactoryImpl.class);

        // topics
        bind(TopicMatcher.class).to(TokenizedTopicMatcher.class);

        // mqtt handling
        final DefaultEventExecutorGroup mqttHandlerWorker = new DefaultEventExecutorGroup(
                MQTT_EVENT_EXECUTOR_THREAD_COUNT.get(),
                new ThreadFactoryBuilder().setNameFormat("hivemq-event-executor-%d").build());
        bind(EventExecutorGroup.class).toInstance(mqttHandlerWorker);
        bind(MessageDroppedService.class).toInstance(injector.getInstance(MessageDroppedService.class));
        bind(MqttServerDisconnector.class).to(MqttServerDisconnectorImpl.class).in(Singleton.class);
        bind(MqttConnacker.class).to(MqttConnackerImpl.class).in(Singleton.class);

        // mqtt service
        bind(InternalPublishService.class).to(InternalPublishServiceImpl.class);
        bind(PublishDistributor.class).to(PublishDistributorImpl.class);
        bind(PublishPollService.class).to(PublishPollServiceImpl.class).in(Singleton.class);

        // throttling
        bind(GlobalTrafficShapingHandler.class).toProvider(GlobalTrafficShapingProvider.class).in(Singleton.class);

        // security
        bind(SslFactory.class).in(Singleton.class);
        bind(SslContextStore.class).in(Singleton.class);

        bind(ScheduledExecutorService.class).annotatedWith(Security.class)
                .toProvider(SecurityExecutorProvider.class)
                .in(Singleton.class);

        // persistence
        install(new LocalPersistenceModule(injector));
        bind(PersistenceShutdownHookInstaller.class).asEagerSingleton();
        bind(ExecutorService.class).annotatedWith(Persistence.class)
                .toProvider(PersistenceExecutorProvider.class)
                .in(Singleton.class);
        bind(ListeningExecutorService.class).annotatedWith(Persistence.class)
                .toProvider(PersistenceExecutorProvider.class)
                .in(Singleton.class);
        bind(ScheduledExecutorService.class).annotatedWith(Persistence.class)
                .toProvider(PersistenceScheduledExecutorProvider.class)
                .in(Singleton.class);
        bind(ListeningScheduledExecutorService.class).annotatedWith(Persistence.class)
                .toProvider(PersistenceScheduledExecutorProvider.class)
                .in(Singleton.class);
        bindIfAbsent(ListeningScheduledExecutorService.class,
                PayloadPersistenceScheduledExecutorProvider.class,
                PayloadPersistence.class);
        bind(TopicTreeStartup.class).asEagerSingleton();
        bind(ScheduledCleanUpService.class).asEagerSingleton();
        requestStaticInjection(FutureUtils.class);

        // metrics
        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(MetricsHolder.class).toInstance(injector.getInstance(MetricsHolder.class));
        bind(SessionsGauge.class).toProvider(SessionsGaugeProvider.class).asEagerSingleton();
        bind(OpenConnectionsGauge.class).toProvider(OpenConnectionsGaugeProvider.class).asEagerSingleton();
        bind(RetainedMessagesGauge.class).toProvider(RetainedMessagesGaugeProvider.class).asEagerSingleton();
        bind(JmxReporterBootstrap.class).asEagerSingleton();
        bind(MetricsShutdownHook.class).asEagerSingleton();
    }

    private void bindIfAbsent(
            final @NotNull Class type,
            final @NotNull Class provider,
            final @NotNull Class annotation) {
        final Object instance = injector.getInstance(Key.get(type, annotation));
        if (instance != null) {
            bind(type).annotatedWith(annotation).toInstance(instance);
        } else {
            bind(type).annotatedWith(annotation).toProvider(provider).in(Singleton.class);
        }
    }


    private static class GlobalTrafficShapingProvider implements Provider<GlobalTrafficShapingHandler> {
        private static final Logger log = LoggerFactory.getLogger(GlobalTrafficShapingProvider.class);
        private static final long outLimit = InternalConfig.OUTGOING_BANDWIDTH_THROTTLING_DEFAULT_BYTES_PER_SEC;


        private final long inLimit;

        @Inject
        GlobalTrafficShapingProvider(final @NotNull RestrictionsConfigService config) {
            inLimit = config.incomingLimit();
            log.debug("Throttling incoming traffic to {} B/s", inLimit);
            log.debug("Throttling outgoing traffic to {} B/s", outLimit);
        }

        @Override
        public @NotNull GlobalTrafficShapingHandler get() {
            final ScheduledExecutorService executor =
                    Executors.newSingleThreadScheduledExecutor(ThreadFactoryUtil.create(
                            "global-traffic-shaper-executor-%d"));
            ShutdownHooks.INSTANCE.add(new ShutdownHooks.Hook() {
                @Override
                public @NotNull String name() {
                    return "Global Traffic Shaper Executor Shutdown Hook";
                }

                @Override
                public @NotNull ShutdownHooks.Priority priority() {
                    return ShutdownHooks.Priority.HIGH;
                }

                @Override
                public void run() {
                    executor.shutdownNow();
                }
            });
            return new GlobalTrafficShapingHandler(executor, outLimit, inLimit, 1000L);
        }
    }
}
