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
package com.hivemq.topics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.util.Bytes;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;


public class SubscriberWithQoS implements Comparable<SubscriberWithQoS> {

    private final @NotNull String subscriber;
    private final int qos;
    private final @Nullable String sharedName;
    private final @Nullable Integer subscriptionId;
    private final @Nullable String topicFilter; // only present for shared subscription
    private final byte flags;

    public SubscriberWithQoS(
            final @NotNull String subscriber,
            final int qos,
            final byte flags,
            final @Nullable Integer subscriptionId) {
        this(subscriber, qos, flags, null, subscriptionId, null);
    }

    public SubscriberWithQoS(
            final @NotNull String subscriber,
            final int qos,
            final byte flags,
            final @Nullable String sharedName,
            final @Nullable Integer subscriptionId,
            final @Nullable String topicFilter) {
        Objects.requireNonNull(subscriber, "Subscriber must not be null");
        checkArgument((qos <= 2 && qos >= 0), "Quality of Service level must be between 0 and 2");
        this.subscriber = subscriber;
        this.qos = qos;
        this.flags = flags;
        this.sharedName = sharedName;
        this.subscriptionId = subscriptionId;
        this.topicFilter = topicFilter;
    }

    @NotNull
    public String getSubscriber() {
        return subscriber;
    }

    public int getQos() {
        return qos;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean isSharedSubscription() {
        return Bytes.isBitSet(flags, SubscriptionFlag.SHARED);
    }

    public boolean isRetainAsPublished() {
        return Bytes.isBitSet(flags, SubscriptionFlag.RETAIN);
    }

    public boolean isNoLocal() {
        return Bytes.isBitSet(flags, SubscriptionFlag.NON_LOCAL);
    }

    @Nullable
    public String getSharedName() {
        return sharedName;
    }

    @Nullable
    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    @Nullable
    public String getTopicFilter() {
        return topicFilter;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscriberWithQoS)) {
            return false;
        }
        final SubscriberWithQoS that = (SubscriberWithQoS) o;
        return qos == that.qos &&
                flags == that.flags &&
                Objects.equals(subscriber, that.subscriber) &&
                Objects.equals(sharedName, that.sharedName) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(topicFilter, that.topicFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriber, qos, flags, sharedName, subscriptionId, topicFilter);
    }

    @Override
    public int compareTo(@Nullable final SubscriberWithQoS that) {
        if (that == null) {
            return -1;
        }
        final int cmp = subscriber.compareTo(that.getSubscriber());
        if (cmp != 0) {
            return cmp;
        }
        final int qosCmp = Integer.compare(qos, that.getQos());
        if (qosCmp == 0 && subscriptionId != null && that.subscriptionId != null) {
            return Integer.compare(subscriptionId, that.subscriptionId);
        }
        return qosCmp;

    }

    @NotNull
    @Override
    public String toString() {
        return "SubscriberWithQoS{" + "subscriber='" + subscriber + '\'' + ", qos=" + qos + '}';
    }
}
