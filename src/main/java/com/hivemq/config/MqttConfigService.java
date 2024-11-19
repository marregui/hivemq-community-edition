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
package com.hivemq.config;

import org.jetbrains.annotations.NotNull;
import com.hivemq.mqtt.message.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


import static com.hivemq.config.ConfigService.KEEP_ALIVE_ALLOW_UNLIMITED_DEFAULT;
import static com.hivemq.config.ConfigService.KEEP_ALIVE_MAX_DEFAULT;
import static com.hivemq.config.ConfigService.MAXIMUM_QOS_DEFAULT;
import static com.hivemq.config.ConfigService.MAX_EXPIRY_INTERVAL_DEFAULT;
import static com.hivemq.config.ConfigService.MAX_QUEUED_MESSAGES_DEFAULT;
import static com.hivemq.config.ConfigService.QUEUED_MESSAGES_STRATEGY_DEFAULT;
import static com.hivemq.config.ConfigService.RETAINED_MESSAGES_ENABLED_DEFAULT;
import static com.hivemq.config.ConfigService.SERVER_RECEIVE_MAXIMUM_DEFAULT;
import static com.hivemq.config.ConfigService.SHARED_SUBSCRIPTIONS_ENABLED_DEFAULT;
import static com.hivemq.config.ConfigService.SUBSCRIPTION_IDENTIFIER_ENABLED_DEFAULT;
import static com.hivemq.config.ConfigService.TOPIC_ALIAS_ENABLED_DEFAULT;
import static com.hivemq.config.ConfigService.TOPIC_ALIAS_MAX_PER_CLIENT_DEFAULT;
import static com.hivemq.config.ConfigService.WILDCARD_SUBSCRIPTIONS_ENABLED_DEFAULT;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.SESSION_EXPIRY_MAX;

@Singleton
public class MqttConfigService {

    private static final Logger log = LoggerFactory.getLogger(MqttConfigService.class);
    private final AtomicLong maxClientSessionExpiryInterval = new AtomicLong(SESSION_EXPIRY_MAX);
    private final AtomicLong maxMessageExpiryInterval = new AtomicLong(MAX_EXPIRY_INTERVAL_DEFAULT);
    private final AtomicInteger serverReceiveMaximum = new AtomicInteger(SERVER_RECEIVE_MAXIMUM_DEFAULT);
    private final AtomicInteger maxPacketSize = new AtomicInteger(DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
    private final AtomicLong maxQueuedMessages = new AtomicLong(MAX_QUEUED_MESSAGES_DEFAULT);
    private final AtomicReference<QueuedMessagesStrategy> queuedMessagesStrategy =
            new AtomicReference<>(QUEUED_MESSAGES_STRATEGY_DEFAULT);
    private final AtomicBoolean retainedMessagesEnabled = new AtomicBoolean(RETAINED_MESSAGES_ENABLED_DEFAULT);
    private final AtomicBoolean wildcardSubscriptionsEnabled =
            new AtomicBoolean(WILDCARD_SUBSCRIPTIONS_ENABLED_DEFAULT);
    private final AtomicBoolean topicAliasEnabled = new AtomicBoolean(TOPIC_ALIAS_ENABLED_DEFAULT);
    private final AtomicInteger topicAliasMaxPerClient = new AtomicInteger(TOPIC_ALIAS_MAX_PER_CLIENT_DEFAULT);
    private final AtomicBoolean subscriptionIdentifierEnabled =
            new AtomicBoolean(SUBSCRIPTION_IDENTIFIER_ENABLED_DEFAULT);
    private final AtomicBoolean sharedSubscriptionsEnabled = new AtomicBoolean(SHARED_SUBSCRIPTIONS_ENABLED_DEFAULT);
    private final AtomicBoolean keepAliveAllowZero = new AtomicBoolean(KEEP_ALIVE_ALLOW_UNLIMITED_DEFAULT);
    private final AtomicInteger keepAliveMax = new AtomicInteger(KEEP_ALIVE_MAX_DEFAULT);
    private final AtomicReference<QoS> maximumQos = new AtomicReference<>(MAXIMUM_QOS_DEFAULT);

    public long maxQueuedMessages() {
        return maxQueuedMessages.get();
    }

    public long maxSessionExpiryInterval() {
        return maxClientSessionExpiryInterval.get();
    }

    public long maxMessageExpiryInterval() {
        return maxMessageExpiryInterval.get();
    }

    public int serverReceiveMaximum() {
        return serverReceiveMaximum.get();
    }

    public int maxPacketSize() {
        return maxPacketSize.get();
    }

    public @NotNull QueuedMessagesStrategy getQueuedMessagesStrategy() {
        return queuedMessagesStrategy.get();
    }

    public void setQueuedMessagesStrategy(@NotNull final QueuedMessagesStrategy strategy) {
        Objects.requireNonNull(strategy, "Queued Messages strategy must not be null");
        log.debug("Setting queued messages strategy for each client to {}", strategy.name());
        queuedMessagesStrategy.set(strategy);
    }

    public boolean retainedMessagesEnabled() {
        return retainedMessagesEnabled.get();
    }

    public boolean wildcardSubscriptionsEnabled() {
        return wildcardSubscriptionsEnabled.get();
    }

    public @NotNull QoS maximumQos() {
        return maximumQos.get();
    }

    public boolean topicAliasEnabled() {
        return topicAliasEnabled.get();
    }

    public int topicAliasMaxPerClient() {
        return topicAliasMaxPerClient.get();
    }

    public boolean subscriptionIdentifierEnabled() {
        return subscriptionIdentifierEnabled.get();
    }

    public boolean sharedSubscriptionsEnabled() {
        return sharedSubscriptionsEnabled.get();
    }

    public boolean keepAliveAllowZero() {
        return keepAliveAllowZero.get();
    }

    public int keepAliveMax() {
        return keepAliveMax.get();
    }

    public void setMaxPacketSize(final int maxPacketSize) {
        log.debug("Setting the maximum packet size for mqtt messages {} bytes", maxPacketSize);
        this.maxPacketSize.set(maxPacketSize);
    }

    public void setMaxQueuedMessages(final long maxQueuedMessages) {
        log.debug("Setting the number of max queued messages  per client to {} entries", maxQueuedMessages);
        this.maxQueuedMessages.set(maxQueuedMessages);
    }

    public void setMaxSessionExpiryInterval(final long maxClientSessionExpiryInterval) {
        log.debug("Setting the expiry interval for client sessions to {} seconds", maxClientSessionExpiryInterval);
        this.maxClientSessionExpiryInterval.set(maxClientSessionExpiryInterval);
    }

    public void setMaxMessageExpiryInterval(final long messageExpiryInterval) {
        log.debug("Setting the expiry interval for publish messages to {} seconds", messageExpiryInterval);
        this.maxMessageExpiryInterval.set(messageExpiryInterval);
    }

    public void setRetainedMessagesEnabled(final boolean enabled) {
        log.debug("Setting retained messages enabled to {}", enabled);
        this.retainedMessagesEnabled.set(enabled);
    }

    public void setWildcardSubscriptionsEnabled(final boolean enabled) {
        log.debug("Setting wildcard subscriptions enabled to {}", enabled);
        this.wildcardSubscriptionsEnabled.set(enabled);
    }

    public void setMaximumQos(@NotNull final QoS maximumQos) {
        Objects.requireNonNull(maximumQos, "Maximum QoS may never be null");
        log.debug("Setting maximum qos to {} ", maximumQos);
        this.maximumQos.set(maximumQos);
    }

    public void setTopicAliasEnabled(final boolean enabled) {
        log.debug("Setting topic alias enabled to {}", enabled);
        this.topicAliasEnabled.set(enabled);
    }

    public void setTopicAliasMaxPerClient(final int maxPerClient) {
        log.debug("Setting topic alias maximum per client to {}", maxPerClient);
        this.topicAliasMaxPerClient.set(maxPerClient);
    }

    public void setSubscriptionIdentifierEnabled(final boolean enabled) {
        log.debug("Setting subscription identifier enabled to {}", enabled);
        this.subscriptionIdentifierEnabled.set(enabled);
    }

    public void setSharedSubscriptionsEnabled(final boolean enabled) {
        log.debug("Setting shared subscriptions enabled to {}", enabled);
        this.sharedSubscriptionsEnabled.set(enabled);
    }

    public void setKeepAliveAllowZero(final boolean allowZero) {
        log.debug("Setting keep alive allow zero to {}", allowZero);
        this.keepAliveAllowZero.set(allowZero);
    }

    public void setKeepAliveMax(final int keepAliveMax) {
        log.debug("Setting keep alive maximum to {} seconds", keepAliveMax);
        this.keepAliveMax.set(keepAliveMax);
    }

    public void setServerReceiveMaximum(final int serverReceiveMaximum) {
        log.debug("Setting the server receive maximum to {}", serverReceiveMaximum);
        this.serverReceiveMaximum.set(serverReceiveMaximum);
    }

    public enum QueuedMessagesStrategy {
        DISCARD_OLDEST(0), // discards the oldest element in the queue if the queue is full
        DISCARD(1); // discards the current element to queue in case the queue is full.

        private static final @NotNull QueuedMessagesStrategy @NotNull [] VALUES = values();

        private final int index;

        QueuedMessagesStrategy(final int index) {
            this.index = index;
        }

        public static @NotNull QueuedMessagesStrategy valueOf(final int index) {
            try {
                return VALUES[index];
            } catch (final ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("No queued messages strategy for index " + index, e);
            }
        }

        public int getIndex() {
            return index;
        }
    }
}
