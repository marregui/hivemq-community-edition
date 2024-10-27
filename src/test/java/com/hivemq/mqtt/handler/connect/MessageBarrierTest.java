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
package com.hivemq.mqtt.handler.connect;

import com.hivemq.bootstrap.ClientConnectionContext;
import com.hivemq.logging.EventLog;
import com.hivemq.mqtt.handler.disconnect.MqttServerDisconnector;
import com.hivemq.mqtt.handler.disconnect.MqttServerDisconnectorImpl;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import util.DummyClientConnection;
import util.DummyHandler;

import static com.hivemq.bootstrap.netty.ChannelHandlerNames.MQTT_MESSAGE_BARRIER;
import static org.junit.Assert.assertEquals;

public class MessageBarrierTest {

    private EmbeddedChannel channel;
    private MessageBarrier messageBarrier;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        final MqttServerDisconnector mqttServerDisconnector = new MqttServerDisconnectorImpl(new EventLog());

        messageBarrier = new MessageBarrier(mqttServerDisconnector);
        channel = new EmbeddedChannel(new DummyHandler());
        channel.attr(ClientConnectionContext.CHANNEL_ATTRIBUTE_NAME).set(new DummyClientConnection(channel, null));
        channel.pipeline().addFirst(MQTT_MESSAGE_BARRIER, messageBarrier);
    }

    @Test
    public void test_default() {
        assertEquals(false, messageBarrier.getConnectReceived());
    }
}
