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

import com.hivemq.config.entity.MqttConfigEntity;
import com.hivemq.config.entity.RestrictionsEntity;
import com.hivemq.config.entity.SecurityConfigEntity;
import com.hivemq.mqtt.message.QoS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

@SuppressWarnings("NullabilityAnnotations")
public class ConfigFileReaderTest {

    ConfigService reader;
    @Mock
    private MqttConfigService mqttConfigService;
    @Mock
    private RestrictionsConfigService restrictionsConfigService;
    @Mock
    private SecurityConfigService securityConfigService;
    @Mock
    private SysInfo sysInfo;
    private ListenerConfigService listenerConfigService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        listenerConfigService = new ListenerConfigService();
        reader = new ConfigService();
    }

    @Test
    public void verify_mqtt_default_values() {

        final MqttConfigEntity defaultMqttValues = new MqttConfigEntity();
        verify(mqttConfigService).setQueuedMessagesStrategy(MqttConfigService.QueuedMessagesStrategy.valueOf(
                defaultMqttValues.getQueuedMessagesConfigEntity().getQueuedMessagesStrategy().name()));
        verify(mqttConfigService).setMaxPacketSize(defaultMqttValues.getPacketsConfigEntity()
                .getMaxPacketSize());
        verify(mqttConfigService).setServerReceiveMaximum(defaultMqttValues.getReceiveMaximumConfigEntity()
                .getServerReceiveMaximum());
        verify(mqttConfigService).setMaxQueuedMessages(defaultMqttValues.getQueuedMessagesConfigEntity()
                .getMaxQueueSize());
        verify(mqttConfigService).setMaxSessionExpiryInterval(defaultMqttValues.getSessionExpiryConfigEntity()
                .getMaxInterval());
        verify(mqttConfigService).setMaxMessageExpiryInterval(defaultMqttValues.getMessageExpiryConfigEntity()
                .getMaxInterval());
        verify(mqttConfigService).setRetainedMessagesEnabled(defaultMqttValues.getRetainedMessagesConfigEntity()
                .isEnabled());
        verify(mqttConfigService).setWildcardSubscriptionsEnabled(defaultMqttValues.getWildcardSubscriptionsConfigEntity()
                .isEnabled());
        verify(mqttConfigService).setMaximumQos(QoS.valueOf(defaultMqttValues.getQoSConfigEntity().getMaxQos()));
        verify(mqttConfigService).setTopicAliasEnabled(defaultMqttValues.getTopicAliasConfigEntity()
                .isEnabled());
        verify(mqttConfigService).setTopicAliasMaxPerClient(defaultMqttValues.getTopicAliasConfigEntity()
                .getMaxPerClient());
        verify(mqttConfigService).setSubscriptionIdentifierEnabled(defaultMqttValues.getSubscriptionIdentifierConfigEntity()
                .isEnabled());
        verify(mqttConfigService).setSharedSubscriptionsEnabled(defaultMqttValues.getSharedSubscriptionsConfigEntity()
                .isEnabled());
        verify(mqttConfigService).setKeepAliveAllowZero(defaultMqttValues.getKeepAliveConfigEntity()
                .isAllowUnlimted());
        verify(mqttConfigService).setKeepAliveMax(defaultMqttValues.getKeepAliveConfigEntity()
                .getMaxKeepAlive());
    }

    @Test
    public void verify_restrictions_default_values() {
        final RestrictionsEntity defaultThrottlingValues = new RestrictionsEntity();

        verify(restrictionsConfigService).setMaxConnections(defaultThrottlingValues.getMaxConnections());
        verify(restrictionsConfigService).setMaxClientIdLength(defaultThrottlingValues.getMaxClientIdLength());
        verify(restrictionsConfigService).setMaxTopicLength(defaultThrottlingValues.getMaxTopicLength());
        verify(restrictionsConfigService).setNoConnectIdleTimeout(defaultThrottlingValues.getNoConnectIdleTimeout());
        verify(restrictionsConfigService).setIncomingLimit(defaultThrottlingValues.getIncomingBandwidthThrottling());
    }

    @Test
    public void verify_security_default_values() {
        final SecurityConfigEntity defaultSecurityValues = new SecurityConfigEntity();

        verify(securityConfigService).setValidateUTF8(defaultSecurityValues.getUtf8ValidationEntity()
                .isEnabled());
        verify(securityConfigService).setPayloadFormatValidation(defaultSecurityValues.getPayloadFormatValidationEntity()
                .isEnabled());
        verify(securityConfigService).setAllowServerAssignedClientId(defaultSecurityValues.getAllowEmptyClientIdEntity()
                .isEnabled());

    }
}
