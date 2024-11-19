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
package util;

import com.hivemq.config.ConfigurationService;
import com.hivemq.config.MqttConfigurationService;
import com.hivemq.config.RestrictionsConfigurationService;
import com.hivemq.config.SecurityConfigurationService;
import com.hivemq.config.ListenerConfigurationService;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class TestConfigurationBootstrap {

    private ConfigurationService configurationService;

    public TestConfigurationBootstrap()  {
        try {
            configurationService = new ConfigurationService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public SecurityConfigurationService getSecurityConfigurationService() {
        return configurationService.securityConfiguration();
    }

    public ConfigurationService getFullConfigurationService() {
        return configurationService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public ListenerConfigurationService getListenerConfigurationService() {
        return configurationService.listenerConfiguration();
    }

    public MqttConfigurationService getMqttConfigurationService() {
        return configurationService.mqttConfiguration();
    }

    public RestrictionsConfigurationService getRestrictionsConfigurationService() {
        return configurationService.restrictionsConfiguration();
    }
}
