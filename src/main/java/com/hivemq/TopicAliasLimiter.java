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

import com.hivemq.config.InternalConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class TopicAliasLimiter {

    private final @NotNull AtomicLong memoryUsage = new AtomicLong();
    private final @NotNull AtomicLong topicAliasesTotal = new AtomicLong();
    private final int memorySoftLimit = InternalConfig.TOPIC_ALIAS_GLOBAL_MEMORY_SOFT_LIMIT_BYTES.get();
    private final int memoryHardLimit = InternalConfig.TOPIC_ALIAS_GLOBAL_MEMORY_HARD_LIMIT_BYTES.get();

    private static int getEstimatedSize(final @NotNull String topic) {
        return 38 + 2 * topic.length();
    }

    public boolean aliasesAvailable() {
        return memoryUsage.get() < memorySoftLimit;
    }

    public boolean limitExceeded() {
        return memoryUsage.get() > memoryHardLimit;
    }

    public void initUsage(final int size) {
        memoryUsage.addAndGet(4L * size); //4 bytes per topic as index
    }

    public void addUsage(@NotNull final String topic) {
        memoryUsage.addAndGet(getEstimatedSize(topic));
        topicAliasesTotal.incrementAndGet();
    }

    public void removeUsage(final @NotNull String @Nullable ... topics) {
        for (int i = 0; i < topics.length; i++) {
            final String topic = topics[i];
            if (topic != null) {
                memoryUsage.addAndGet(-1 * getEstimatedSize(topic));
                topicAliasesTotal.decrementAndGet();
            }
        }
    }

    public void finishUsage(final @NotNull String @Nullable ... topics) {
        memoryUsage.addAndGet(-4L * topics.length); //4 bytes per topic as index
        removeUsage(topics);
    }
}
