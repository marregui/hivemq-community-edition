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

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import com.hivemq.topics.SubscriberWithIds;

public class TopicSubscribers {

    private final @NotNull ImmutableSet<SubscriberWithIds> subscriber;
    private final @NotNull ImmutableSet<String> sharedSubscriptions;

    public TopicSubscribers(
            final @NotNull ImmutableSet<SubscriberWithIds> subscriber,
            final @NotNull ImmutableSet<String> sharedSubscriptions) {
        this.subscriber = subscriber;
        this.sharedSubscriptions = sharedSubscriptions;
    }

    public @NotNull ImmutableSet<SubscriberWithIds> getSubscribers() {
        return subscriber;
    }

    public @NotNull ImmutableSet<String> getSharedSubscriptions() {
        return sharedSubscriptions;
    }
}
