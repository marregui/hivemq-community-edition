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

package com.hivemq.configuration.service;

import com.google.common.collect.ImmutableList;
import com.hivemq.UnrecoverableException;
import com.hivemq.configuration.entity.HiveMQConfigEntity;
import com.hivemq.configuration.entity.listener.TCPListenerEntity;
import com.hivemq.configuration.entity.listener.TlsTCPListenerEntity;
import com.hivemq.configuration.entity.listener.TlsWebsocketListenerEntity;
import com.hivemq.configuration.entity.listener.WebsocketListenerEntity;
import com.hivemq.configuration.info.SystemInformation;
import com.hivemq.configuration.reader.*;
import com.hivemq.configuration.service.impl.MqttConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.RestrictionsConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.SecurityConfigurationServiceImpl;
import com.hivemq.configuration.service.impl.listener.ListenerConfigurationService;
import com.hivemq.configuration.service.impl.listener.ListenerConfigurationServiceImpl;
import com.hivemq.util.EnvVarUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private final @NotNull File configFile;
    private final @NotNull ListenerConfigurationService listenerConfig;
    private final @NotNull MqttConfigurationService mqttConfig;
    private final @NotNull RestrictionsConfigurationService restrictionsConfig;
    private final @NotNull SecurityConfigurationService securityConfig;

    private final @NotNull ListenerConfigurator listenerConfigurator;
    private final @NotNull MqttConfigurator mqttConfigurator;
    private final @NotNull RestrictionConfigurator restrictionConfigurator;
    private final @NotNull SecurityConfigurator securityConfigurator;

    public ConfigurationService() {
        listenerConfig = new ListenerConfigurationServiceImpl();
        mqttConfig = new MqttConfigurationServiceImpl();
        restrictionsConfig = new RestrictionsConfigurationServiceImpl();
        securityConfig = new SecurityConfigurationServiceImpl();

        this.listenerConfigurator =new ListenerConfigurator(listenerConfig);
        this.mqttConfigurator = new MqttConfigurator(mqttConfig);
        this.restrictionConfigurator = new RestrictionConfigurator(restrictionsConfig);
        this.securityConfigurator = new SecurityConfigurator(securityConfig);

        File file = new File(SystemInformation.INSTANCE.getConfigFolder(), "config.xml");
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            log.error("Cannot access config file {}. Using defaults", file.getAbsolutePath());
            file = null;
        }
        this.configFile = file;
        if (configFile != null) {
            final File configFile = this.configFile;
            log.debug("Reading configuration file {}", configFile);

            try {
                final Class<?>[] classes = ImmutableList.<Class<?>>builder()
                        .add(getConfigEntityClass())
                        .addAll(getInheritedEntityClasses())
                        .build()
                        .toArray(new Class<?>[0]);

                final JAXBContext context = JAXBContext.newInstance(classes);
                final Unmarshaller unmarshaller = context.createUnmarshaller();

                //replace environment variable placeholders
                String configFileContent = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
                configFileContent = new EnvVarUtil().replaceEnvironmentVariablePlaceholders(configFileContent);
                final ByteArrayInputStream is =
                        new ByteArrayInputStream(configFileContent.getBytes(StandardCharsets.UTF_8));
                final StreamSource streamSource = new StreamSource(is);

                setConfiguration(unmarshaller.unmarshal(streamSource, getConfigEntityClass()).getValue());

            } catch (final Exception e) {
                if (e.getCause() instanceof UnrecoverableException) {
                    log.error("An unrecoverable Exception occurred. Exiting HiveMQ", e);
                    log.debug("Original error message:", e);
                    System.exit(1);
                }
                log.error("Could not read the configuration file {}. Using default config",
                        configFile.getAbsolutePath());
                log.debug("Original error message:", e);
                setConfiguration(getDefaultConfig());
            }
        } else {
            setConfiguration(getDefaultConfig());
        }
    }

    void setConfiguration(@NotNull final HiveMQConfigEntity config) {
        listenerConfigurator.setListenerConfig(config.getListenerConfig());
        mqttConfigurator.setMqttConfig(config.getMqttConfig());
        restrictionConfigurator.setRestrictionsConfig(config.getRestrictionsConfig());
        securityConfigurator.setSecurityConfig(config.getSecurityConfig());
    }

    @NotNull HiveMQConfigEntity getDefaultConfig() {
        return new HiveMQConfigEntity();
    }

    @NotNull Class<? extends HiveMQConfigEntity> getConfigEntityClass() {
        return HiveMQConfigEntity.class;
    }

    @NotNull List<Class<?>> getInheritedEntityClasses() {
        return ImmutableList.of(
                /* ListenerEntity */
                TCPListenerEntity.class,
                WebsocketListenerEntity.class,
                TlsTCPListenerEntity.class,
                TlsWebsocketListenerEntity.class);
    }

    public @NotNull ListenerConfigurationService listenerConfiguration() {
        return listenerConfig;
    }

    public @NotNull MqttConfigurationService mqttConfiguration() {
        return mqttConfig;
    }

    public @NotNull RestrictionsConfigurationService restrictionsConfiguration() {
        return restrictionsConfig;
    }

    public @NotNull SecurityConfigurationService securityConfiguration() {
        return securityConfig;
    }
}
