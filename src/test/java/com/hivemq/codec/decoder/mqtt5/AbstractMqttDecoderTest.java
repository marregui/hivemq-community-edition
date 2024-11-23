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
package com.hivemq.codec.decoder.mqtt5;

import com.hivemq.bootstrap.ClientConnection;
import com.hivemq.bootstrap.Connection;
import org.jetbrains.annotations.NotNull;
import com.hivemq.mqtt.message.ProtocolVersion;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import util.DummyClientConnection;
import util.TestMqttDecoder;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class AbstractMqttDecoderTest {

    protected @NotNull ProtocolVersion protocolVersion;
    protected @NotNull EmbeddedChannel channel;
    protected @NotNull ClientConnection clientConnection;

    @Before
    public void setUp() throws JAXBException, IOException {
        channel = new EmbeddedChannel(TestMqttDecoder.create());
        clientConnection = new DummyClientConnection(channel, null);
        clientConnection.setProtocolVersion(protocolVersion);
        channel.attr(Connection.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);
    }

    @After
    public void tearDown() {
        channel.close();
    }

    protected void createChannel() throws JAXBException, IOException {
        channel = new EmbeddedChannel(TestMqttDecoder.create());
        clientConnection = new DummyClientConnection(channel, null);
        clientConnection.setProtocolVersion(protocolVersion);
        channel.attr(Connection.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);
    }
}
