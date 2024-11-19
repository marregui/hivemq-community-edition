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

import com.google.common.io.Files;
import com.hivemq.mqtt.message.QoS;
import org.junit.Test;

import static com.hivemq.config.ConfigService.MAX_EXPIRY_INTERVAL_DEFAULT;
import static com.hivemq.config.ConfigService.TOPIC_ALIAS_MAX_PER_CLIENT_MAXIMUM;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.SESSION_EXPIRE_ON_DISCONNECT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

public class MqttValidatorTest extends AbstractConfigurationTest {

    @Test
    public void test_server_receive_max_negative_xml() throws Exception {

        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                "<receive-maximum> " +
                "<server-receive-maximum>-1</server-receive-maximum> " +
                "</receive-maximum> " +
                "    </mqtt\n>" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        // Default is 10
        assertEquals(10, mqttConfigService.serverReceiveMaximum());
    }

    @Test
    public void test_mqtt_xml() throws Exception {

        final String contents = "<hivemq>" +
                "<mqtt> " +
                "<retained-messages> " +
                "<enabled>false</enabled> " +
                "</retained-messages> " +
                "<wildcard-subscriptions> " +
                "<enabled>false</enabled> " +
                "</wildcard-subscriptions> " +
                "<quality-of-service> " +
                "<max-qos>1</max-qos> " +
                "</quality-of-service> " +
                "<topic-alias> " +
                "<enabled>true</enabled> " +
                "<max-per-client>5</max-per-client> " +
                "</topic-alias> " +
                "<message-expiry> " +
                "<max-interval>3600</max-interval> " +
                "</message-expiry> " +
                "<session-expiry> " +
                "<max-interval>3600</max-interval> " +
                "</session-expiry> " +
                "<subscription-identifier> " +
                "<enabled>true</enabled> " +
                "</subscription-identifier> " +
                "<queued-messages> " +
                "<max-queue-size>100</max-queue-size> " +
                "<strategy>discard-oldest</strategy> " +
                "</queued-messages> " +
                "<shared-subscriptions> " +
                "<enabled>false</enabled> " +
                "</shared-subscriptions> " +
                "<keep-alive> " +
                "<allow-unlimited>false</allow-unlimited> " +
                "<max-keep-alive>65</max-keep-alive> " +
                "</keep-alive> " +
                "<packets> " +
                "<max-packet-size>2684</max-packet-size> " +
                "</packets> " +
                "<receive-maximum> " +
                "<server-receive-maximum>120</server-receive-maximum> " +
                "</receive-maximum> " +
                "</mqtt> " +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);


        assertEquals(100, mqttConfigService.maxQueuedMessages());
        assertEquals(3600, mqttConfigService.maxSessionExpiryInterval());
        assertEquals(3600, mqttConfigService.maxMessageExpiryInterval());
        assertEquals(120, mqttConfigService.serverReceiveMaximum());
        assertEquals(2684, mqttConfigService.maxPacketSize());
        assertEquals(
                MqttConfigService.QueuedMessagesStrategy.DISCARD_OLDEST,
                mqttConfigService.getQueuedMessagesStrategy());
        assertFalse(mqttConfigService.retainedMessagesEnabled());
        assertFalse(mqttConfigService.wildcardSubscriptionsEnabled());
        assertEquals(QoS.AT_LEAST_ONCE, mqttConfigService.maximumQos());
        assertTrue(mqttConfigService.topicAliasEnabled());
        assertEquals(5, mqttConfigService.topicAliasMaxPerClient());
        assertTrue(mqttConfigService.subscriptionIdentifierEnabled());
        assertFalse(mqttConfigService.sharedSubscriptionsEnabled());
        assertFalse(mqttConfigService.keepAliveAllowZero());
        assertEquals(65, mqttConfigService.keepAliveMax());

    }

    @Test
    public void test_topic_alias_min_values() throws Exception {

        final String contents = "<hivemq>" +
                "<mqtt> " +
                "<topic-alias> " +
                "<enabled>true</enabled> " +
                "<max-per-client>0</max-per-client> " +
                "</topic-alias> " +
                "</mqtt> " +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertTrue(mqttConfigService.topicAliasEnabled());
        assertEquals(1, mqttConfigService.topicAliasMaxPerClient());

    }

    @Test
    public void test_topic_alias_max_values() throws Exception {

        final String contents = "<hivemq>" +
                "<mqtt> " +
                "<topic-alias> " +
                "<enabled>true</enabled> " +
                "<max-per-client>70000</max-per-client> " +
                "</topic-alias> " +
                "</mqtt> " +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertTrue(mqttConfigService.topicAliasEnabled());
        assertEquals(TOPIC_ALIAS_MAX_PER_CLIENT_MAXIMUM, mqttConfigService.topicAliasMaxPerClient());

    }


    @Test
    public void test_server_receive_max_to_large_xml() throws Exception {

        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                "<receive-maximum> " +
                "<server-receive-maximum>70000</server-receive-maximum> " +
                "</receive-maximum> " +
                "    </mqtt\n>" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertEquals(65535, mqttConfigService.serverReceiveMaximum());
    }

    @Test
    public void test_mqtt_xml_env_var() throws Exception {
        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                "<queued-messages> " +
                "<max-queue-size>${MAX_QUEUED_MESSAGES}</max-queue-size> " +
                "</queued-messages> " +
                "    </mqtt\n>" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertEquals(3, mqttConfigService.maxQueuedMessages());
    }

    @Test
    public void test_ttl_check() throws Exception {

        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                "<message-expiry> " +
                "<max-interval>0</max-interval> " +
                "</message-expiry> " +
                "<session-expiry> " +
                "<max-interval>-1</max-interval> " +
                "</session-expiry> " +
                "    </mqtt\n>" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertEquals(SESSION_EXPIRE_ON_DISCONNECT, mqttConfigService.maxSessionExpiryInterval());
        assertEquals(MAX_EXPIRY_INTERVAL_DEFAULT, mqttConfigService.maxMessageExpiryInterval());
    }


    @Test
    public void test_max_packet_size_max() throws Exception {

        final int maxPacketSize = 268435460;
        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                "<packets> " +
                "<max-packet-size>" +
                maxPacketSize +
                "</max-packet-size> " +
                "</packets> " +
                "</mqtt>\n" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertEquals(maxPacketSize, mqttConfigService.maxPacketSize());
    }

    @Test
    public void test_max_packet_size_min() throws Exception {

        final int maxPacketSize = 1;
        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                "<packets> " +
                "<max-packet-size>" +
                maxPacketSize +
                "</max-packet-size> " +
                "</packets> " +
                "</mqtt>\n" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        assertEquals(maxPacketSize, mqttConfigService.maxPacketSize());
    }

    @Test
    public void test_max_packet_size_gt_max() throws Exception {

        final int maxPacketSize = 268435461;
        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                // Set max packet size + 1
                "<packets> " +
                "<max-packet-size>" +
                maxPacketSize +
                "</max-packet-size> " +
                "</packets> " +
                "</mqtt>\n" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        // We expect the default to be set -> 268435460 and not 268435461
        assertEquals(DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT, mqttConfigService.maxPacketSize());
    }

    @Test
    public void test_max_packet_size_negative() throws Exception {

        final int maxPacketSize = -1;
        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                // Set max packet size to -1
                "<packets> " +
                "<max-packet-size>" +
                maxPacketSize +
                "</max-packet-size> " +
                "</packets> " +
                "</mqtt>\n" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        // We expect the default to be set -> 268435460 and not -1
        assertEquals(268435460, mqttConfigService.maxPacketSize());
    }

    @Test
    public void test_max_packet_size_zero() throws Exception {

        final int maxPacketSize = 0;
        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                // Set max packet size to 0
                "<packets> " +
                "<max-packet-size>" +
                maxPacketSize +
                "</max-packet-size> " +
                "</packets> " +
                "</mqtt>\n" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        // We expect the default to be set -> 268435460 and not 0
        assertEquals(268435460, mqttConfigService.maxPacketSize());
    }

    @Test
    public void test_max_packet_size_string() throws Exception {

        final String contents = "<hivemq>" +
                " <mqtt>\n" +
                // Set max packet size to 'i am a string'
                "<packets> " +
                "<max-packet-size>i am a string</max-packet-size> " +
                "</packets> " +
                "</mqtt>\n" +
                "</hivemq>";
        Files.write(contents.getBytes(UTF_8), xmlFile);

        // We expect the default to be set -> 268435460 and not 'im a string'
        assertEquals(268435460, mqttConfigService.maxPacketSize());
    }

}
