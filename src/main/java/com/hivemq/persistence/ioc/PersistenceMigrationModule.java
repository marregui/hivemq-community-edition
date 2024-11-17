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
package com.hivemq.persistence.ioc;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hivemq.bootstrap.SingletonModule;
import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import org.jetbrains.annotations.NotNull;
import com.hivemq.metrics.MetricsHolder;
import com.hivemq.metrics.ioc.provider.MetricsHolderProvider;
import com.hivemq.mqtt.message.dropping.MessageDroppedService;
import com.hivemq.mqtt.message.dropping.MessageDroppedServiceProvider;
import com.hivemq.persistence.PersistenceStartup;
import com.hivemq.persistence.PersistenceStartupShutdownHookInstaller;
import com.hivemq.persistence.ioc.annotation.PayloadPersistence;
import com.hivemq.persistence.ioc.provider.local.PayloadPersistenceScheduledExecutorProvider;
import com.hivemq.persistence.payload.PublishPayloadPersistence;
import com.hivemq.persistence.payload.PublishPayloadPersistenceImpl;

import javax.inject.Singleton;

public class PersistenceMigrationModule extends SingletonModule<Class<PersistenceMigrationModule>> {

    private final @NotNull MetricRegistry metricRegistry;

    public PersistenceMigrationModule(@NotNull final MetricRegistry metricRegistry) {
        super(PersistenceMigrationModule.class);
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected void configure() {
        bind(PersistenceStartup.class).asEagerSingleton();
        bind(PersistenceStartupShutdownHookInstaller.class).asEagerSingleton();

        install(new PersistenceMigrationFileModule());

        bind(PublishPayloadPersistence.class).to(PublishPayloadPersistenceImpl.class);

        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(MetricsHolder.class).toProvider(MetricsHolderProvider.class).asEagerSingleton();

        bind(ListeningScheduledExecutorService.class).annotatedWith(PayloadPersistence.class)
                .toProvider(PayloadPersistenceScheduledExecutorProvider.class)
                .in(LazySingleton.class);

        bind(MessageDroppedService.class).toProvider(MessageDroppedServiceProvider.class).in(Singleton.class);
    }
}
