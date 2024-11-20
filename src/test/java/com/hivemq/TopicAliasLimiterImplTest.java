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


import com.hivemq.config.RandomId;
import com.hivemq.config.InternalConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TopicAliasLimiterImplTest {

    private TopicAliasLimiter topicAliasLimiter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        InternalConfig.TOPIC_ALIAS_GLOBAL_MEMORY_SOFT_LIMIT_BYTES.set(50);
        InternalConfig.TOPIC_ALIAS_GLOBAL_MEMORY_HARD_LIMIT_BYTES.set(200);

        topicAliasLimiter = new TopicAliasLimiter();
    }

    @Test
    public void test_init_usage() {

        topicAliasLimiter.initUsage(5);

        assertFalse(topicAliasLimiter.limitExceeded());
        assertTrue(topicAliasLimiter.aliasesAvailable());

        topicAliasLimiter.initUsage(5);

        assertFalse(topicAliasLimiter.limitExceeded());
        assertTrue(topicAliasLimiter.aliasesAvailable());

        topicAliasLimiter.initUsage(5);

        assertFalse(topicAliasLimiter.limitExceeded());
        assertFalse(topicAliasLimiter.aliasesAvailable());

    }

    @Test
    public void test_add_usage() {

        topicAliasLimiter.addUsage(RandomId.randomAlphanumeric(6));
        assertFalse(topicAliasLimiter.limitExceeded());
        assertFalse(topicAliasLimiter.aliasesAvailable());

        topicAliasLimiter.addUsage(RandomId.randomAlphanumeric(56));

        assertFalse(topicAliasLimiter.limitExceeded());
        assertFalse(topicAliasLimiter.aliasesAvailable());

        topicAliasLimiter.addUsage(RandomId.randomAlphanumeric(1));

        assertTrue(topicAliasLimiter.limitExceeded());
        assertFalse(topicAliasLimiter.aliasesAvailable());

    }

    @Test
    public void test_remove_usage() {

        topicAliasLimiter.addUsage(RandomId.randomAlphanumeric(107));

        topicAliasLimiter.removeUsage(RandomId.randomAlphanumeric(6));
        assertTrue(topicAliasLimiter.limitExceeded());
        assertFalse(topicAliasLimiter.aliasesAvailable());

        topicAliasLimiter.removeUsage(RandomId.randomAlphanumeric(1));

        assertFalse(topicAliasLimiter.limitExceeded());
        assertFalse(topicAliasLimiter.aliasesAvailable());

        topicAliasLimiter.removeUsage(RandomId.randomAlphanumeric(151));

        assertFalse(topicAliasLimiter.limitExceeded());
        assertTrue(topicAliasLimiter.aliasesAvailable());
    }

    @Test
    public void test_finish_usage() {

        final String topic = RandomId.randomAlphanumeric(6);

        topicAliasLimiter.initUsage(5);

        topicAliasLimiter.addUsage(topic);
        topicAliasLimiter.addUsage(topic);
        topicAliasLimiter.addUsage(topic);
        topicAliasLimiter.addUsage(topic);
        topicAliasLimiter.addUsage(topic);

        topicAliasLimiter.finishUsage(topic, topic, topic, topic, topic);

        assertFalse(topicAliasLimiter.limitExceeded());
        assertTrue(topicAliasLimiter.aliasesAvailable());

    }
}
