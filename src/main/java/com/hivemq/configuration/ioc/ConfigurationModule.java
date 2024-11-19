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
package com.hivemq.configuration.ioc;

import com.hivemq.bootstrap.SingletonModule;
import com.hivemq.configuration.HivemqId;
import com.hivemq.configuration.service.ConfigurationService;
import com.hivemq.configuration.service.ConfigurationService;
import com.hivemq.configuration.service.MqttConfigurationService;
import com.hivemq.configuration.service.RestrictionsConfigurationService;
import com.hivemq.configuration.service.SecurityConfigurationService;
import com.hivemq.configuration.service.impl.listener.ListenerConfigurationService;
import org.jetbrains.annotations.NotNull;

public class ConfigurationModule extends SingletonModule<Class<ConfigurationModule>> {

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull HivemqId hiveMQId;

    public ConfigurationModule(
            final @NotNull ConfigurationService configurationService) {
        super(ConfigurationModule.class);
        this.configurationService = configurationService;
        this.hiveMQId = new HivemqId();
    }

    public @NotNull String getHiveMQId() {
        return hiveMQId.get();
    }

    @Override
    protected void configure() {
        bind(HivemqId.class).toInstance(hiveMQId);
        bind(ListenerConfigurationService.class).toInstance(configurationService.listenerConfiguration());
        bind(MqttConfigurationService.class).toInstance(configurationService.mqttConfiguration());
        bind(RestrictionsConfigurationService.class).toInstance(configurationService.restrictionsConfiguration());
        bind(ConfigurationService.class).toInstance(configurationService);
        bind(ConfigurationService.class).toInstance(configurationService);
        bind(SecurityConfigurationService.class).toInstance(configurationService.securityConfiguration());
    }
}
