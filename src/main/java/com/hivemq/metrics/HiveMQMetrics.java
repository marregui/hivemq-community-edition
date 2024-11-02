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
package com.hivemq.metrics;

import com.codahale.metrics.Counter;

import static com.hivemq.metrics.HiveMQMetric.Gauge;


public class HiveMQMetrics {

    public static final HiveMQMetric<Counter> INCOMING_MESSAGE_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.incoming.total.count", Counter.class);
    public static final HiveMQMetric<Counter> OUTGOING_MESSAGE_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.outgoing.total.count", Counter.class);
    public static final HiveMQMetric<Counter> INCOMING_CONNECT_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.incoming.connect.count", Counter.class);
    public static final HiveMQMetric<Counter> INCOMING_PUBLISH_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.incoming.publish.count", Counter.class);
    public static final HiveMQMetric<Counter> OUTGOING_PUBLISH_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.outgoing.publish.count", Counter.class);
    public static final HiveMQMetric<Counter> DROPPED_MESSAGE_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.dropped.count", Counter.class);
    public static final HiveMQMetric<Gauge> RETAINED_MESSAGES_CURRENT =
            HiveMQMetric.gaugeValue("com.hivemq.messages.retained.current");
    public static final HiveMQMetric<Gauge> BYTES_READ_TOTAL =
            HiveMQMetric.gaugeValue("com.hivemq.networking.bytes.read.total");
    public static final HiveMQMetric<Gauge> BYTES_WRITE_TOTAL =
            HiveMQMetric.gaugeValue("com.hivemq.networking.bytes.write.total");
    public static final HiveMQMetric<Gauge> CONNECTIONS_OVERALL_CURRENT =
            HiveMQMetric.gaugeValue("com.hivemq.networking.connections.current");
    public static final HiveMQMetric<Counter> CONNECTIONS_CLOSED_COUNT =
            HiveMQMetric.valueOf("com.hivemq.networking.connections-closed.total.count", Counter.class);
    public static final HiveMQMetric<Counter> SUBSCRIPTIONS_CURRENT =
            HiveMQMetric.valueOf("com.hivemq.subscriptions.overall.current", Counter.class);
    public static final HiveMQMetric<Gauge> CLIENT_SESSIONS_CURRENT =
            HiveMQMetric.gaugeValue("com.hivemq.sessions.overall.current");
    public static final HiveMQMetric<Counter> MQTT_CONNECTION_NOT_WRITABLE_CURRENT =
            HiveMQMetric.valueOf("com.hivemq.mqtt.connection.not-writable.current", Counter.class);
    public static final HiveMQMetric<Counter> WILL_MESSAGE_COUNT =
            HiveMQMetric.valueOf("com.hivemq.messages.will.count.current", Counter.class);
    public static final HiveMQMetric<Counter> WILL_MESSAGE_PUBLISHED_COUNT_TOTAL =
            HiveMQMetric.valueOf("com.hivemq.messages.will.published.count.total", Counter.class);
}

