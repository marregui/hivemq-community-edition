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
package com.hivemq.topics.tree;

import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;
import com.hivemq.mqtt.message.subscribe.Topic;
import com.hivemq.topics.SubscriptionFlag;
import com.hivemq.persistence.clientsession.ClientSession;
import com.hivemq.persistence.clientsession.ClientSessionPersistence;
import com.hivemq.persistence.clientsession.ClientSessionSubscriptionPersistence;
import com.hivemq.persistence.clientsession.SharedSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.inject.Singleton;

import java.util.Set;

import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.SESSION_EXPIRE_ON_DISCONNECT;
import static com.hivemq.persistence.clientsession.SharedSubscriptionService.SharedSubscription;

@Singleton
public class TopicTreeStartup {

    private static final @NotNull Logger log = LoggerFactory.getLogger(TopicTreeStartup.class);

    private final @NotNull TopicTree topicTree;
    private final @NotNull ClientSessionPersistence session;
    private final @NotNull ClientSessionSubscriptionPersistence subscriptions;

    @Inject
    TopicTreeStartup(
            final @NotNull TopicTree topicTree,
            final @NotNull ClientSessionPersistence session,
            final @NotNull ClientSessionSubscriptionPersistence subscriptions) {
        this.topicTree = topicTree;
        this.session = session;
        this.subscriptions = subscriptions;
    }

    @PostConstruct
    void postConstruct() {
        final ListenableFuture<Set<String>> clientsFuture = session.getAllClients();
        try {
            for (final String client : clientsFuture.get()) {
                final ClientSession session = this.session.getSession(client, false);
                if (session == null || session.getSessionExpiryIntervalSec() == SESSION_EXPIRE_ON_DISCONNECT) {
                    subscriptions.removeAllLocally(client);
                    continue;
                }
                for (final Topic topic : subscriptions.getSubscriptions(client)) {
                    final SharedSubscription shared =
                            SharedSubscriptionService.checkForSharedSubscription(topic.getTopic());
                    if (shared == null) {
                        topicTree.addTopic(client,
                                topic,
                                SubscriptionFlag.buildFlag(false, topic.isRetainAsPublished(), topic.isNoLocal()),
                                null);
                    } else {
                        topicTree.addTopic(client,
                                new Topic(shared.getTopicFilter(),
                                        topic.getQoS(),
                                        topic.isNoLocal(),
                                        topic.isRetainAsPublished()),
                                SubscriptionFlag.buildFlag(true, topic.isRetainAsPublished(), topic.isNoLocal()),
                                shared.getShareName());
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("Failed to bootstrap topic tree.", ex);
        }
    }
}
