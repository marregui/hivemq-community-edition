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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.readString;

public class ConfigurationService {

    private static final @NotNull Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private final @NotNull ListenerConfigurationService listenerConfig;
    private final @NotNull MqttConfigurationService mqttConfig;
    private final @NotNull RestrictionsConfigurationService restrictionsConfig;
    private final @NotNull SecurityConfigurationService securityConfig;

    private final @NotNull ListenerConfigurator listenerConfigurator;
    private final @NotNull MqttConfigurator mqttConfigurator;
    private final @NotNull RestrictionConfigurator restrictionConfigurator;
    private final @NotNull SecurityConfigurator securityConfigurator;

    public ConfigurationService() throws IOException, JAXBException {
        listenerConfig = new ListenerConfigurationServiceImpl();
        mqttConfig = new MqttConfigurationServiceImpl();
        restrictionsConfig = new RestrictionsConfigurationServiceImpl();
        securityConfig = new SecurityConfigurationServiceImpl();
        listenerConfigurator = new ListenerConfigurator(listenerConfig);
        mqttConfigurator = new MqttConfigurator(mqttConfig);
        restrictionConfigurator = new RestrictionConfigurator(restrictionsConfig);
        securityConfigurator = new SecurityConfigurator(securityConfig);
        final File file = new File(SystemInformation.INSTANCE.getConfigFolder(), "config.xml");
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            log.error("Cannot read config {}. Using defaults", file.getAbsolutePath());
            setConfig(new HiveMQConfigEntity());
        } else {
            log.debug("Reading config {}", file.getAbsolutePath());
            setConfig(JAXBContext.newInstance(HiveMQConfigEntity.class,
                            TCPListenerEntity.class,
                            WebsocketListenerEntity.class,
                            TlsTCPListenerEntity.class,
                            TlsWebsocketListenerEntity.class)
                    .createUnmarshaller()
                    .unmarshal(readFileContent(file), HiveMQConfigEntity.class)
                    .getValue());
        }
    }

    private static @NotNull StreamSource readFileContent(final @NotNull File file) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final Matcher env = Pattern.compile("\\$\\{(ENV:)*(.*?)}").matcher(readString(file.toPath()));
        while (env.find()) {
            if (env.groupCount() < 2) {
                log.warn("unexpected env");
                env.appendReplacement(sb, "");
                continue;
            }
            final String varName = env.group(2);
            final String prop = System.getProperty(varName);
            final String varValue = prop != null ? prop : System.getenv(varName);
            if (varValue == null) {
                log.error("Env {} not set.", varName);
                throw new UnrecoverableException();
            }
            env.appendReplacement(sb, varValue.replace("\\", "\\\\").replace("$", "\\$"));
        }
        env.appendTail(sb);
        return new StreamSource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void setConfig(final @NotNull HiveMQConfigEntity config) {
        listenerConfigurator.setListenerConfig(config.getListenerConfig());
        mqttConfigurator.setMqttConfig(config.getMqttConfig());
        restrictionConfigurator.setRestrictionsConfig(config.getRestrictionsConfig());
        securityConfigurator.setSecurityConfig(config.getSecurityConfig());
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
