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
package com.hivemq.extensions.packets.general;

import org.junit.Test;

import static com.hivemq.extension.sdk.api.packets.general.MqttVersion.V_5;
import static com.hivemq.mqtt.message.ProtocolVersion.MQTTv5;
import static org.junit.Assert.assertSame;


public class MqttVersionUtilTest {

    @Test(timeout = 5000)
    public void test_version_5() {
        assertSame(V_5, MqttVersionUtil.toMqttVersion(MQTTv5));
        assertSame(MQTTv5, MqttVersionUtil.toProtocolVersion(V_5));
    }
}
