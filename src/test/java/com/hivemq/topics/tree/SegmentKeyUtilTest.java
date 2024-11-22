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

import com.hivemq.util.Strings;
import org.junit.Test;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class SegmentKeyUtilTest {
    private static String firstSegmentKey(final String topic) {
        return  topic.isEmpty()? "" :  segmentKey(topic, 1);
    }

    private static String segmentKey(final String topic, final int length) {
        Objects.requireNonNull(topic, "Topic must not be null");
        checkArgument(!topic.isEmpty(), "Topic must not be empty");
        checkArgument(length > 0, "Segment key length must be grater than zero");
        int end = -1;
        for (int i = 0; i < length; i++) {
            end = topic.indexOf('/', end + 1);
            if (end == -1) {
                return topic;
            }
        }
        return topic.substring(0, end);
    }

    @Test
    public void test_segnemt_key_util() {
        assertEquals("topic", segmentKey("topic", 1));
        assertEquals("topic", segmentKey("topic", 2));
        assertEquals("topic", segmentKey("topic/1", 1));
        assertEquals("topic/1", segmentKey("topic/1", 2));
        assertEquals("topic/1", segmentKey("topic/1", 3));
        assertEquals("topic/1", segmentKey("topic/1/2", 2));
        assertEquals("topic/", segmentKey("topic//", 2));
        assertEquals("topic//", segmentKey("topic//", 3));
        assertEquals("/topic", segmentKey("/topic", 2));
        assertEquals("", segmentKey("/topic", 1));
    }

    @Test
    public void name() {
        assertEquals("", segmentKey("/topic", 1));
    }

    @Test
    public void test_first_segment_key() {
        assertEquals("topic", firstSegmentKey("topic"));
        assertEquals("topic", firstSegmentKey("topic/1"));
        assertEquals("topic", firstSegmentKey("topic/"));
        assertEquals("", firstSegmentKey("/topic"));
    }

    @Test
    public void test_contains_wildcard() {
        assertFalse(Strings.containsNotWildcard("topic/+"));
        assertFalse(Strings.containsNotWildcard("topic/#"));
        assertFalse(Strings.containsNotWildcard("+/topic"));
        assertFalse(Strings.containsNotWildcard("+"));
        assertFalse(Strings.containsNotWildcard("#"));
        assertFalse(Strings.containsNotWildcard("/#"));
        assertFalse(Strings.containsNotWildcard("/+"));
        assertTrue(Strings.containsNotWildcard("topic"));
    }
}
