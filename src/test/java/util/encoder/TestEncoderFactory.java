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
package util.encoder;

import com.hivemq.bootstrap.Connection;
import com.hivemq.codec.encoder.EncoderFactory;
import com.hivemq.codec.encoder.MqttEncoder;
import com.hivemq.config.SecurityConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.mqtt.handler.disconnect.MqttServerDisconnector;
import com.hivemq.mqtt.message.Message;
import com.hivemq.mqtt.message.PINGREQ;
import com.hivemq.mqtt.message.dropping.MessageDroppedService;

/**
 * @author Abdullah Imal
 */
public class TestEncoderFactory extends EncoderFactory {

    private final @NotNull PingreqEncoder pingreqEncoder;

    public TestEncoderFactory(
            final @NotNull MessageDroppedService messageDroppedService,
            final @NotNull SecurityConfigurationService securityConfigurationService,
            final @NotNull MqttServerDisconnector mqttServerDisconnector,
            final @NotNull PingreqEncoder pingreqEncoder) {

        super(messageDroppedService, securityConfigurationService, mqttServerDisconnector);

        this.pingreqEncoder = pingreqEncoder;
    }

    @Override
    protected @Nullable MqttEncoder getEncoder(
            final @NotNull Message msg, final @NotNull Connection clientConnectionContext) {
        if (msg instanceof PINGREQ) {
            return pingreqEncoder;
        }
        return super.getEncoder(msg, clientConnectionContext);
    }
}
