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

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockitoAnnotations;

import java.io.File;


public class AbstractConfigurationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    ListenerConfigService listenerConfigService;
    File xmlFile;
    MqttConfigService mqttConfigService;
    RestrictionsConfigService restrictionsConfigService;
    SecurityConfigService securityConfigService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        listenerConfigService = new ListenerConfigService();

        xmlFile = temporaryFolder.newFile();
        securityConfigService = new SecurityConfigService();
        mqttConfigService = new MqttConfigService();
        restrictionsConfigService = new RestrictionsConfigService();
    }
}
