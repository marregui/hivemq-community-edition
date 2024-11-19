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
package com.hivemq.configuration.reader;

import com.hivemq.configuration.service.MqttConfigurationService;
import com.hivemq.configuration.service.RestrictionsConfigurationService;
import com.hivemq.configuration.service.SecurityConfigurationService;
import com.hivemq.configuration.service.impl.MqttConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.RestrictionsConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.SecurityConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.listener.ListenerConfigurationService;
import com.hivemq.configuration.service.impl.listener.ListenerConfigurationServiceImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockitoAnnotations;

import java.io.File;


public class AbstractConfigurationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    ListenerConfigurationService listenerConfigurationService;
    File xmlFile;
    MqttConfigurationService mqttConfigurationService;
    RestrictionsConfigurationService restrictionsConfigurationService;
    SecurityConfigurationService securityConfigurationService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        listenerConfigurationService = new ListenerConfigurationServiceImpl();

        xmlFile = temporaryFolder.newFile();
        securityConfigurationService = new SecurityConfigurationServiceImpl();
        mqttConfigurationService = new MqttConfigurationServiceImpl();
        restrictionsConfigurationService = new RestrictionsConfigurationServiceImpl();
    }
}
