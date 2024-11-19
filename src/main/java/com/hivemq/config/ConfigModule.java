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

import com.hivemq.bootstrap.SingletonModule;
import org.jetbrains.annotations.NotNull;

public class ConfigModule extends SingletonModule<Class<ConfigModule>> {

    private final @NotNull ConfigService configService;
    private final @NotNull HivemqId hiveMQId;

    public ConfigModule(
            final @NotNull ConfigService configService) {
        super(ConfigModule.class);
        this.configService = configService;
        this.hiveMQId = new HivemqId();
    }

    public @NotNull String getHiveMQId() {
        return hiveMQId.get();
    }

    @Override
    protected void configure() {
        bind(HivemqId.class).toInstance(hiveMQId);
        bind(ListenerConfigurationService.class).toInstance(configService.listenerConfiguration());
        bind(MqttConfigurationService.class).toInstance(configService.mqttConfiguration());
        bind(RestrictionsConfigurationService.class).toInstance(configService.restrictionsConfiguration());
        bind(ConfigService.class).toInstance(configService);
        bind(ConfigService.class).toInstance(configService);
        bind(SecurityConfigurationService.class).toInstance(configService.securityConfiguration());
    }
}
