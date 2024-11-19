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

import com.google.inject.Injector;
import com.hivemq.bootstrap.SingletonModule;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import com.hivemq.persistence.PersistenceStartup;
import com.hivemq.persistence.clientqueue.ClientQueuePersistence;
import com.hivemq.persistence.clientqueue.ClientQueuePersistenceImpl;
import com.hivemq.persistence.clientsession.ClientSessionPersistence;
import com.hivemq.persistence.clientsession.ClientSessionPersistenceProvider;
import com.hivemq.persistence.clientsession.ClientSessionSubscriptionPersistence;
import com.hivemq.persistence.clientsession.ClientSessionSubscriptionPersistenceProvider;
import com.hivemq.persistence.connection.ConnectionPersistence;
import com.hivemq.persistence.connection.ConnectionPersistenceImpl;
import com.hivemq.persistence.ioc.provider.local.IncomingMessageFlowPersistenceLocalProvider;
import com.hivemq.persistence.local.IncomingMessageFlowLocalPersistence;
import com.hivemq.persistence.payload.PublishPayloadPersistence;
import com.hivemq.persistence.payload.PublishPayloadPersistenceImpl;
import com.hivemq.persistence.qos.IncomingMessageFlowPersistence;
import com.hivemq.persistence.qos.IncomingMessageFlowPersistenceImpl;
import com.hivemq.persistence.retained.RetainedMessagePersistence;
import com.hivemq.persistence.retained.RetainedMessagePersistenceProvider;

public class LocalPersistenceModule extends SingletonModule<LocalPersistenceModule> {

    private final @NotNull Injector persistenceInjector;

    public LocalPersistenceModule(@NotNull final Injector persistenceInjector) {
        super(LocalPersistenceModule.class);
        this.persistenceInjector = persistenceInjector;
    }

    @Override
    protected void configure() {

        install(new LocalPersistenceFileModule(persistenceInjector));


        /* Retained Message */
        bind(RetainedMessagePersistence.class).toProvider(RetainedMessagePersistenceProvider.class)
                .in(Singleton.class);

        /* Connection */
        bind(ConnectionPersistence.class).to(ConnectionPersistenceImpl.class).in(Singleton.class);

        /* Client Session */
        bind(ClientSessionPersistence.class).toProvider(ClientSessionPersistenceProvider.class).in(Singleton.class);

        /* Client Session Sub */
        bind(ClientSessionSubscriptionPersistence.class).toProvider(ClientSessionSubscriptionPersistenceProvider.class)
                .in(Singleton.class);

        /* QoS Handling */
        bind(IncomingMessageFlowPersistence.class).to(IncomingMessageFlowPersistenceImpl.class);
        bind(IncomingMessageFlowLocalPersistence.class).toProvider(IncomingMessageFlowPersistenceLocalProvider.class)
                .in(Singleton.class);

        /* Client Queue */
        bind(ClientQueuePersistence.class).to(ClientQueuePersistenceImpl.class).in(Singleton.class);

        /* Payload Persistence */
        bind(PublishPayloadPersistence.class).toInstance(persistenceInjector.getInstance(PublishPayloadPersistence.class));
        bind(PublishPayloadPersistenceImpl.class).toInstance(persistenceInjector.getInstance(
                PublishPayloadPersistenceImpl.class));

        /* Startup */
        bind(PersistenceStartup.class).toInstance(persistenceInjector.getInstance(PersistenceStartup.class));
    }
}
