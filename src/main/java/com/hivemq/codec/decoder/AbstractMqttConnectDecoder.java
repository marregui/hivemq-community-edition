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
package com.hivemq.codec.decoder;

import com.hivemq.bootstrap.Connection;
import com.hivemq.config.ConfigurationService;
import com.hivemq.config.InternalConfigurations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.mqtt.handler.connack.MqttConnacker;
import com.hivemq.mqtt.message.connect.CONNECT;
import com.hivemq.mqtt.message.reason.Mqtt5ConnAckReasonCode;
import com.hivemq.util.ClientIds;
import com.hivemq.util.ReasonStrings;
import com.hivemq.util.Strings;
import io.netty.buffer.ByteBuf;

import static com.hivemq.util.Bytes.isBitSet;

/**
 * An Abstract Class for all Mqtt CONNECT Decoders
 *
 * @author Florian Limpöck
 */
public abstract class AbstractMqttConnectDecoder extends MqttDecoder<CONNECT> {

    protected static final int DISCONNECTED = -1;
    private static final byte VARIABLE_HEADER_LENGTH = 10;

    protected final @NotNull MqttConnacker mqttConnacker;
    protected final @NotNull ClientIds clientIds;
    protected final long maxSessionExpiryInterval;
    protected final long maxUserPropertiesLength;
    protected final boolean allowAssignedClientId;
    protected final boolean validateUTF8;

    protected AbstractMqttConnectDecoder(
            final @NotNull MqttConnacker mqttConnacker,
            final @NotNull ConfigurationService configurationService,
            final @NotNull ClientIds clientIds) {
        this.mqttConnacker = mqttConnacker;
        this.clientIds = clientIds;
        validateUTF8 = configurationService.securityConfiguration().validateUTF8();
        maxUserPropertiesLength = InternalConfigurations.USER_PROPERTIES_MAX_SIZE_BYTES;
        maxSessionExpiryInterval = configurationService.mqttConfiguration().maxSessionExpiryInterval();
        allowAssignedClientId = configurationService.securityConfiguration().allowServerAssignedClientId();
    }

    protected void disconnectByInvalidFixedHeader(final @NotNull Connection clientConnectionContext) {
        mqttConnacker.connackError(clientConnectionContext.getChannel(),
                "A client (IP: {}) connected with an invalid fixed header.",
                "Invalid CONNECT fixed header",
                Mqtt5ConnAckReasonCode.MALFORMED_PACKET,
                ReasonStrings.CONNACK_MALFORMED_PACKET_FIXED_HEADER);
    }

    protected void disconnectByInvalidHeader(final @NotNull Connection clientConnectionContext) {
        mqttConnacker.connackError(clientConnectionContext.getChannel(),
                "A client (ID: {},IP: {}) connected with an invalid CONNECT header.",
                "Invalid CONNECT header",
                Mqtt5ConnAckReasonCode.PROTOCOL_ERROR,
                ReasonStrings.CONNACK_PROTOCOL_ERROR_VARIABLE_HEADER);
    }

    /**
     * Validates Will Flags by MQTT Specification.
     * <p>
     * If the Will Flag is set to 0, then the Will QoS MUST be set to 0. If the Will Flag is set to 0, then Will Retain
     * MUST be set to 0
     * <p>
     * If the Will Flag is set to 1, the value of Will QoS can be 0, 1, or 2.
     * <p>
     * A Will QoS of 3 is a malformed packet.
     * <p>
     * A Will QoS of 0 and a Will Retain of 1 is a malformed packet.
     *
     * @param isWillFlag              will flag set
     * @param isWillRetain            will retain set
     * @param willQoS                 quality of service of will
     * @param clientConnectionContext the connection of the mqtt client
     * @return true if valid, else false
     */
    protected boolean validateWill(
            final boolean isWillFlag,
            final boolean isWillRetain,
            final int willQoS,
            final @NotNull Connection clientConnectionContext) {

        final boolean valid = (isWillFlag && willQoS < 3) || (!isWillRetain && willQoS == 0);
        if (!valid) {
            mqttConnacker.connackError(clientConnectionContext.getChannel(),
                    "A client (IP: {}) connected with an invalid willTopic flag combination. Disconnecting client.",
                    "Invalid will-topic/flag combination",
                    Mqtt5ConnAckReasonCode.MALFORMED_PACKET,
                    ReasonStrings.CONNACK_MALFORMED_WILL_FLAG);
        }
        return valid;
    }

    /**
     * Validates the connect flags byte
     * <p>
     * If the first bit of the connect flags byte is set, it is a malformed packet
     *
     * @param connectFlagsByte        the connect flags byte
     * @param clientConnectionContext the connection of the mqtt client
     * @return false if the reserved bit zero is set to 1, else true
     */
    protected boolean validateConnectFlagByte(
            final byte connectFlagsByte, final @NotNull Connection clientConnectionContext) {

        if (isBitSet(connectFlagsByte, 0)) {
            mqttConnacker.connackError(clientConnectionContext.getChannel(),
                    "A client (IP: {}) connected with invalid CONNECT flags. Disconnecting client.",
                    "Invalid CONNECT flags",
                    Mqtt5ConnAckReasonCode.MALFORMED_PACKET,
                    ReasonStrings.CONNACK_MALFORMED_CONNECT_FLAGS);
            return false;
        }
        return true;
    }

    /**
     * Validates the protocol name of the MQTT Version
     * <p>
     * If the validation fails it is an unsupported protocol version error.
     *
     * @param variableHeader          the variable header of a mqtt connect message
     * @param clientConnectionContext the connection of the mqtt client
     * @return true if valid, else false
     */
    protected boolean validateProtocolName(
            final @NotNull ByteBuf variableHeader,
            final @NotNull Connection clientConnectionContext,
            final @NotNull String protocolName) {

        if (!protocolName.equals(Strings.getPrefixedString(variableHeader))) {
            mqttConnacker.connackError(clientConnectionContext.getChannel(),
                    "A client (IP: {}) connected with an invalid protocol name. Disconnecting client.",
                    "Invalid CONNECT protocol name",
                    Mqtt5ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION,
                    ReasonStrings.CONNACK_UNSUPPORTED_PROTOCOL_VERSION);
            variableHeader.clear();
            return false;
        }
        return true;
    }

    /**
     * THIS IS FOR MQTT 3 ONLY !
     * <p>
     * checks if the username flag is set when the password flag is set
     */
    protected boolean validateUsernamePassword(final boolean isUsernameFlag, final boolean isPasswordFlag) {
        //Validates that the username flag is set if the password flag is set
        return !isPasswordFlag || isUsernameFlag;
    }

    /**
     * decodes the fixed part of the connect variable header.
     * <p>
     * the buf reader index will be increased by the length
     * <p>
     * if readable bytes less than length a connack with protocol error reason code will be sent.
     *
     * @param clientConnectionContext the connection of the mqtt client
     * @param buf                     the encoded ByteBuf of the message
     * @return a new ByteBuf of the fixed variable header part or {@code null} in an error case
     */
    protected @Nullable ByteBuf decodeFixedVariableHeaderConnect(
            final @NotNull Connection clientConnectionContext, final @NotNull ByteBuf buf) {

        if (buf.readableBytes() >= VARIABLE_HEADER_LENGTH) {
            return buf.readSlice(VARIABLE_HEADER_LENGTH);
        } else {
            disconnectByInvalidHeader(clientConnectionContext);
            return null;
        }
    }
}
