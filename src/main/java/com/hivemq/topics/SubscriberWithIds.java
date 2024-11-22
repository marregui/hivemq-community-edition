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

import com.google.common.primitives.ImmutableIntArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.util.Bytes;

import java.util.Objects;



public class SubscriberWithIds implements Comparable<SubscriberWithIds> {

    private final @NotNull String subscriber;
    private final byte flags;
    private final @Nullable String sharedName;
    private final @Nullable String topicFilter; // only present for shared subscription
    private int qos;
    private @NotNull ImmutableIntArray subscriptionIds;

    public SubscriberWithIds(
            final @NotNull String subscriber, final int qos, final byte flags) {
        this(subscriber, qos, flags, null, null, ImmutableIntArray.of());

    }

    public SubscriberWithIds(
            final @NotNull String subscriber,
            final int qos,
            final byte flags,
            final @Nullable String sharedName,
            final @Nullable String topicFilter,
            final @NotNull ImmutableIntArray subscriptionIds) {
        Objects.requireNonNull(subscriber, "Subscriber must not be null");
        this.subscriber = subscriber;
        this.qos = qos;
        this.flags = flags;
        this.sharedName = sharedName;
        this.topicFilter = topicFilter;
        this.subscriptionIds = subscriptionIds;
    }


    public SubscriberWithIds(final @NotNull SubscriberWithQoS subscriber) {
        this(subscriber.getSubscriber(),
                subscriber.getQos(),
                subscriber.getFlags(),
                subscriber.getSharedName(),
                subscriber.getTopicFilter(),
                subscriber.getSubscriptionId() != null ?
                        ImmutableIntArray.of(subscriber.getSubscriptionId()) :
                        ImmutableIntArray.of());
    }

    @Override
    public int compareTo(final SubscriberWithIds o) {
        final int cmp = subscriber.compareTo(o.getSubscriber());
        if (cmp == 0) {
            return Integer.compare(qos, o.getQos());
        }
        return cmp;
    }

    public @NotNull String getSubscriber() {
        return subscriber;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(final int qos) {
        this.qos = qos;
    }

    public byte getFlags() {
        return flags;
    }

    public @Nullable String getSharedName() {
        return sharedName;
    }

    public @NotNull ImmutableIntArray getSubscriptionIds() {
        return subscriptionIds;
    }

    public void setSubscriptionIds(final @NotNull ImmutableIntArray subscriptionIds) {
        this.subscriptionIds = subscriptionIds;
    }

    public boolean isSharedSubscription() {
        return Bytes.isBitSet(flags, SubscriptionFlag.SHARED_SUBSCRIPTION.getOffset());
    }

    public boolean isRetainAsPublished() {
        return Bytes.isBitSet(flags, SubscriptionFlag.RETAIN_AS_PUBLISHED.getOffset());
    }

    public boolean isNoLocal() {
        return Bytes.isBitSet(flags, SubscriptionFlag.NO_LOCAL.getOffset());
    }

    public @Nullable String getTopicFilter() {
        return topicFilter;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SubscriberWithIds that = (SubscriberWithIds) o;
        return qos == that.qos &&
                flags == that.flags &&
                Objects.equals(subscriber, that.subscriber) &&
                Objects.equals(sharedName, that.sharedName) &&
                Objects.equals(subscriptionIds, that.subscriptionIds) &&
                Objects.equals(topicFilter, that.topicFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriber, qos, flags, sharedName, subscriptionIds, topicFilter);
    }
}
