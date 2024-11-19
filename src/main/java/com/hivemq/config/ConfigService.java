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

import com.hivemq.UnrecoverableException;
import com.hivemq.codec.encoder.mqtt5.UnsignedDataTypes;
import com.hivemq.config.entity.ClientAuthenticationModeEntity;
import com.hivemq.config.entity.HiveMQConfigEntity;
import com.hivemq.config.entity.Listener;
import com.hivemq.config.entity.ListenerEntity;
import com.hivemq.config.entity.MqttConfigEntity;
import com.hivemq.config.entity.RestrictionsEntity;
import com.hivemq.config.entity.SecurityConfigEntity;
import com.hivemq.config.entity.TCPListenerEntity;
import com.hivemq.config.entity.TLSEntity;
import com.hivemq.config.entity.TcpListener;
import com.hivemq.config.entity.Tls;
import com.hivemq.config.entity.TlsTCPListenerEntity;
import com.hivemq.config.entity.TlsTcpListener;
import com.hivemq.config.entity.TlsWebsocketListener;
import com.hivemq.config.entity.TlsWebsocketListenerEntity;
import com.hivemq.config.entity.WebsocketListener;
import com.hivemq.config.entity.WebsocketListenerEntity;
import com.hivemq.mqtt.message.QoS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hivemq.config.RestrictionsConfigurationService.INCOMING_BANDWIDTH_THROTTLING_DEFAULT;
import static com.hivemq.config.RestrictionsConfigurationService.INCOMING_BANDWIDTH_THROTTLING_MINIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_CLIENT_ID_LENGTH_DEFAULT;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_CLIENT_ID_LENGTH_MAXIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_CLIENT_ID_LENGTH_MINIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_CONNECTIONS_DEFAULT;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_CONNECTIONS_MINIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_TOPIC_LENGTH_DEFAULT;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_TOPIC_LENGTH_MAXIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.MAX_TOPIC_LENGTH_MINIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.NO_CONNECT_IDLE_TIMEOUT_DEFAULT;
import static com.hivemq.config.RestrictionsConfigurationService.NO_CONNECT_IDLE_TIMEOUT_MINIMUM;
import static com.hivemq.config.RestrictionsConfigurationService.UNLIMITED_CONNECTIONS;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.DEFAULT_RECEIVE_MAXIMUM;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.SESSION_EXPIRE_ON_DISCONNECT;
import static com.hivemq.mqtt.message.connect.Mqtt5CONNECT.SESSION_EXPIRY_MAX;
import static java.nio.file.Files.readString;

public class ConfigService {

    public static final int TTL_DISABLED = -1;
    public static final int SERVER_RECEIVE_MAXIMUM_DEFAULT = 10;
    public static final long MAX_QUEUED_MESSAGES_DEFAULT = 1000;
    public static final MqttConfigurationService.QueuedMessagesStrategy QUEUED_MESSAGES_STRATEGY_DEFAULT =
            MqttConfigurationService.QueuedMessagesStrategy.DISCARD;
    public static final long MAX_EXPIRY_INTERVAL_DEFAULT = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE + 1;
    public static final boolean RETAINED_MESSAGES_ENABLED_DEFAULT = true;
    public static final QoS MAXIMUM_QOS_DEFAULT = QoS.EXACTLY_ONCE;
    public static final int TOPIC_ALIAS_MAX_PER_CLIENT_DEFAULT = 5;
    public static final int TOPIC_ALIAS_MAX_PER_CLIENT_MINIMUM = 1;
    public static final int TOPIC_ALIAS_MAX_PER_CLIENT_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    public static final boolean TOPIC_ALIAS_ENABLED_DEFAULT = true;
    public static final boolean WILDCARD_SUBSCRIPTIONS_ENABLED_DEFAULT = true;
    public static final boolean SHARED_SUBSCRIPTIONS_ENABLED_DEFAULT = true;
    public static final boolean SUBSCRIPTION_IDENTIFIER_ENABLED_DEFAULT = true;
    public static final boolean KEEP_ALIVE_ALLOW_UNLIMITED_DEFAULT = true;
    public static final int KEEP_ALIVE_MAX_DEFAULT = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    private static final @NotNull Logger log = LoggerFactory.getLogger(ConfigService.class);
    private static final @NotNull String JKS = "JKS";

    private final @NotNull ListenerConfigurationService listener;
    private final @NotNull MqttConfigurationService mqtt;
    private final @NotNull RestrictionsConfigurationService restrictions;
    private final @NotNull SecurityConfigurationService security;
    private final @NotNull List<String> listenerNames;

    public ConfigService() throws IOException, JAXBException {
        listener = new ListenerConfigurationService();
        mqtt = new MqttConfigurationService();
        restrictions = new RestrictionsConfigurationService();
        security = new SecurityConfigurationService();
        listenerNames = new ArrayList<>();
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

    private static @Nullable String getPath(final @NotNull String path) {
        if (path.isBlank()) {
            return null;
        } else {
            final File file = new File(path);
            if (file.isAbsolute()) {
                return file.getAbsolutePath();
            } else {
                return new File(SystemInformation.INSTANCE.getHiveMQHomeFolder(), path).getAbsolutePath();
            }
        }
    }

    private static long validateMaxConnections(final long maxConnections) {
        if (maxConnections == UNLIMITED_CONNECTIONS) {
            return maxConnections;
        }
        if (maxConnections < MAX_CONNECTIONS_MINIMUM) {
            log.warn(
                    "The configured max-connections ({}) must be at least {}. The default value (unlimited) is used instead.",
                    maxConnections,
                    MAX_CONNECTIONS_MINIMUM);
            return MAX_CONNECTIONS_DEFAULT;
        }
        return maxConnections;
    }

    private static int validateMaxClientIdLength(final int maxClientIdLength) {
        if (maxClientIdLength < MAX_CLIENT_ID_LENGTH_MINIMUM || maxClientIdLength > MAX_CLIENT_ID_LENGTH_MAXIMUM) {
            log.warn(
                    "The configured max-client-id-length ({}) must be in the range {} - {}. The default value ({}) is used instead.",
                    maxClientIdLength,
                    MAX_CLIENT_ID_LENGTH_MINIMUM,
                    MAX_CLIENT_ID_LENGTH_MAXIMUM,
                    MAX_CLIENT_ID_LENGTH_DEFAULT);
            return MAX_CLIENT_ID_LENGTH_DEFAULT;
        }
        return maxClientIdLength;
    }

    private static int validateMaxTopicLength(final int maxTopicLength) {
        if (maxTopicLength < MAX_TOPIC_LENGTH_MINIMUM || maxTopicLength > MAX_TOPIC_LENGTH_MAXIMUM) {
            log.warn(
                    "The configured max-topic-length ({}) must be in the range {} - {}. The default value ({}) is used instead.",
                    maxTopicLength,
                    MAX_TOPIC_LENGTH_MINIMUM,
                    MAX_TOPIC_LENGTH_MAXIMUM,
                    MAX_TOPIC_LENGTH_DEFAULT);
            return MAX_TOPIC_LENGTH_DEFAULT;
        }
        return maxTopicLength;
    }

    private static long validateNoConnectIdleTimeout(final long noConnectIdleTimeout) {
        if (noConnectIdleTimeout < NO_CONNECT_IDLE_TIMEOUT_MINIMUM) {
            log.warn(
                    "The configured no-connect-idle-timeout ({}ms) must be at least {}ms. The default value ({}ms) is used instead.",
                    noConnectIdleTimeout,
                    NO_CONNECT_IDLE_TIMEOUT_MINIMUM,
                    NO_CONNECT_IDLE_TIMEOUT_DEFAULT);
            return NO_CONNECT_IDLE_TIMEOUT_DEFAULT;
        }
        return noConnectIdleTimeout;
    }

    private static long validateIncomingLimit(final long incomingLimit) {
        if (incomingLimit < INCOMING_BANDWIDTH_THROTTLING_MINIMUM) {
            log.warn(
                    "The configured incoming-bandwidth-throttling ({} bytes/second) must be at least {} bytes/second. The default value (unlimited) is used instead.",
                    incomingLimit,
                    INCOMING_BANDWIDTH_THROTTLING_MINIMUM);
            return INCOMING_BANDWIDTH_THROTTLING_DEFAULT;
        }
        return incomingLimit;
    }

    private static int validateMaxPerClient(final int maxPerClient) {
        if (maxPerClient < TOPIC_ALIAS_MAX_PER_CLIENT_MINIMUM) {
            log.warn("The configured topic alias maximum per client ({}) is too small. It was set to {} instead.",
                    maxPerClient,
                    TOPIC_ALIAS_MAX_PER_CLIENT_MINIMUM);
            return TOPIC_ALIAS_MAX_PER_CLIENT_MINIMUM;
        }
        if (maxPerClient > TOPIC_ALIAS_MAX_PER_CLIENT_MAXIMUM) {
            log.warn("The configured topic alias maximum per client ({}) is too large. It was set to {} instead.",
                    maxPerClient,
                    TOPIC_ALIAS_MAX_PER_CLIENT_MAXIMUM);
            return TOPIC_ALIAS_MAX_PER_CLIENT_MAXIMUM;
        }
        return maxPerClient;
    }

    private static @NotNull QoS validateQoS(final int qos) {
        final QoS qoS = QoS.valueOf(qos);
        if (qoS != null) {
            return qoS;
        } else {
            log.warn("The configured maximum qos ({}) does not exist. It was set to ({}) instead.",
                    qos,
                    MAXIMUM_QOS_DEFAULT.getQosNumber());
            return MAXIMUM_QOS_DEFAULT;
        }
    }

    private static long validateMessageExpiryInterval(final long maxMessageExpiryInterval) {
        if (maxMessageExpiryInterval <= 0) {
            log.warn("The configured max message expiry interval ({}) is too short. It was set to {} seconds instead.",
                    maxMessageExpiryInterval,
                    MAX_EXPIRY_INTERVAL_DEFAULT);
            return MAX_EXPIRY_INTERVAL_DEFAULT;
        }
        if (maxMessageExpiryInterval > MAX_EXPIRY_INTERVAL_DEFAULT) {
            log.warn("The configured max message expiry interval ({}) is too high. It was set to {} seconds instead.",
                    maxMessageExpiryInterval,
                    MAX_EXPIRY_INTERVAL_DEFAULT);
            return MAX_EXPIRY_INTERVAL_DEFAULT;
        }
        return maxMessageExpiryInterval;
    }

    private static int validateMaxPacketSize(final int maxPacketSize) {
        if (maxPacketSize < 1) {
            log.warn("The configured max packet size ({}) is too short. It was set to {} bytes instead.",
                    maxPacketSize,
                    DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
            return DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
        }
        if (maxPacketSize > DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT) {
            log.warn("The configured max packet size ({}) is too high. It was set to {} bytes instead.",
                    maxPacketSize,
                    DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
            return DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
        }
        return maxPacketSize;
    }

    private static long validateSessionExpiryInterval(final long sessionExpiryInterval) {
        if (sessionExpiryInterval < SESSION_EXPIRE_ON_DISCONNECT) {
            log.warn("The configured session expiry interval ({}) is too short. It was set to {} seconds instead.",
                    sessionExpiryInterval,
                    SESSION_EXPIRE_ON_DISCONNECT);
            return SESSION_EXPIRE_ON_DISCONNECT;
        }
        if (sessionExpiryInterval > SESSION_EXPIRY_MAX) {
            log.warn("The configured session expiry interval ({}) is too high. It was set to {} seconds instead.",
                    sessionExpiryInterval,
                    SESSION_EXPIRY_MAX);
            return SESSION_EXPIRY_MAX;
        }
        return sessionExpiryInterval;
    }

    private static int validateServerReceiveMaximum(final int receiveMaximum) {
        if (receiveMaximum < 1) {
            log.warn("The configured server receive maximum ({}) is too short. It was set to {} seconds instead.",
                    receiveMaximum,
                    SERVER_RECEIVE_MAXIMUM_DEFAULT);
            return SERVER_RECEIVE_MAXIMUM_DEFAULT;
        }
        if (receiveMaximum > DEFAULT_RECEIVE_MAXIMUM) {
            log.warn("The configured server receive maximum ({}) is too high. It was set to {} seconds instead.",
                    receiveMaximum,
                    DEFAULT_RECEIVE_MAXIMUM);
            return DEFAULT_RECEIVE_MAXIMUM;
        }
        return receiveMaximum;
    }

    private static int validateKeepAliveMaximum(final int keepAliveMaximum) {
        if (keepAliveMaximum < 1) {
            log.warn("The configured keep alive maximum ({}) is too short. It was set to {} seconds instead.",
                    keepAliveMaximum,
                    KEEP_ALIVE_MAX_DEFAULT);
            return KEEP_ALIVE_MAX_DEFAULT;
        }
        if (keepAliveMaximum > KEEP_ALIVE_MAX_DEFAULT) {
            log.warn("The configured keep alive maximum ({}) is too high. It was set to {} seconds instead.",
                    keepAliveMaximum,
                    KEEP_ALIVE_MAX_DEFAULT);
            return KEEP_ALIVE_MAX_DEFAULT;
        }
        return keepAliveMaximum;
    }

    private static @NotNull Tls convertTls(final @NotNull TLSEntity entity) {
        return new Tls.Builder().withKeystorePath(Objects.requireNonNull(getPath(entity.getKeystoreEntity().getPath())))
                .withKeystoreType(JKS)
                .withKeystorePassword(entity.getKeystoreEntity().getPassword())
                .withPrivateKeyPassword(entity.getKeystoreEntity().getPrivateKeyPassword())
                .withProtocols(entity.getProtocols())
                .withTruststorePath(getPath(entity.getTruststoreEntity().getPath()))
                .withTruststoreType(JKS)
                .withTruststorePassword(entity.getTruststoreEntity().getPassword())
                .withClientAuthMode(getClientAuthMode(entity.getClientAuthMode()))
                .withCipherSuites(entity.getCipherSuites())
                .withPreferServerCipherSuites(entity.isPreferServerCipherSuites())
                .withHandshakeTimeout(entity.getHandshakeTimeout())
                .build();
    }

    private static @NotNull Tls.ClientAuthMode getClientAuthMode(final @NotNull ClientAuthenticationModeEntity entity) {
        switch (entity) {
            case OPTIONAL:
                return Tls.ClientAuthMode.OPTIONAL;
            case REQUIRED:
                return Tls.ClientAuthMode.REQUIRED;
            case NONE:
                return Tls.ClientAuthMode.NONE;
            default:
                //This should never happen
                return Tls.ClientAuthMode.NONE;
        }
    }

    private void setConfig(final @NotNull HiveMQConfigEntity config) {
        config.getListenerConfig().forEach(entity -> listener.addListener(convertListener(entity)));

        final MqttConfigEntity mqttEntity = config.getMqttConfig();
        mqtt.setRetainedMessagesEnabled(mqttEntity.getRetainedMessagesConfigEntity().isEnabled());
        mqtt.setWildcardSubscriptionsEnabled(mqttEntity.getWildcardSubscriptionsConfigEntity().isEnabled());
        mqtt.setSubscriptionIdentifierEnabled(mqttEntity.getSubscriptionIdentifierConfigEntity().isEnabled());
        mqtt.setSharedSubscriptionsEnabled(mqttEntity.getSharedSubscriptionsConfigEntity().isEnabled());
        mqtt.setMaximumQos(validateQoS(mqttEntity.getQoSConfigEntity().getMaxQos()));
        mqtt.setTopicAliasEnabled(mqttEntity.getTopicAliasConfigEntity().isEnabled());
        mqtt.setTopicAliasMaxPerClient(validateMaxPerClient(mqttEntity.getTopicAliasConfigEntity().getMaxPerClient()));
        mqtt.setMaxQueuedMessages(mqttEntity.getQueuedMessagesConfigEntity().getMaxQueueSize());
        mqtt.setQueuedMessagesStrategy(MqttConfigurationService.QueuedMessagesStrategy.valueOf(mqttEntity.getQueuedMessagesConfigEntity()
                .getQueuedMessagesStrategy()
                .name()));
        mqtt.setMaxSessionExpiryInterval(validateSessionExpiryInterval(mqttEntity.getSessionExpiryConfigEntity()
                .getMaxInterval()));
        mqtt.setMaxMessageExpiryInterval(validateMessageExpiryInterval(mqttEntity.getMessageExpiryConfigEntity()
                .getMaxInterval()));
        mqtt.setServerReceiveMaximum(validateServerReceiveMaximum(mqttEntity.getReceiveMaximumConfigEntity()
                .getServerReceiveMaximum()));
        mqtt.setKeepAliveMax(validateKeepAliveMaximum(mqttEntity.getKeepAliveConfigEntity().getMaxKeepAlive()));
        mqtt.setKeepAliveAllowZero(mqttEntity.getKeepAliveConfigEntity().isAllowUnlimted());
        mqtt.setMaxPacketSize(validateMaxPacketSize(mqttEntity.getPacketsConfigEntity().getMaxPacketSize()));

        final RestrictionsEntity restrictionsEntity = config.getRestrictionsConfig();
        restrictions.setMaxConnections(validateMaxConnections(restrictionsEntity.getMaxConnections()));
        restrictions.setMaxClientIdLength(validateMaxClientIdLength(restrictionsEntity.getMaxClientIdLength()));
        restrictions.setNoConnectIdleTimeout(validateNoConnectIdleTimeout(restrictionsEntity.getNoConnectIdleTimeout()));
        restrictions.setIncomingLimit(validateIncomingLimit(restrictionsEntity.getIncomingBandwidthThrottling()));
        restrictions.setMaxTopicLength(validateMaxTopicLength(restrictionsEntity.getMaxTopicLength()));

        final SecurityConfigEntity securityEntity = config.getSecurityConfig();
        security.setAllowServerAssignedClientId(securityEntity.getAllowEmptyClientIdEntity().isEnabled());
        security.setValidateUTF8(securityEntity.getUtf8ValidationEntity().isEnabled());
        security.setPayloadFormatValidation(securityEntity.getPayloadFormatValidationEntity().isEnabled());
        security.setAllowRequestProblemInformation(securityEntity.getAllowRequestProblemInformationEntity()
                .isEnabled());
    }

    public @NotNull ListenerConfigurationService listenerConfiguration() {
        return listener;
    }

    public @NotNull MqttConfigurationService mqttConfiguration() {
        return mqtt;
    }

    public @NotNull RestrictionsConfigurationService restrictionsConfiguration() {
        return restrictions;
    }

    public @NotNull SecurityConfigurationService securityConfiguration() {
        return security;
    }

    private @NotNull Listener convertListener(final @NotNull ListenerEntity entity) {
        if (entity instanceof TCPListenerEntity) {
            return new TcpListener(entity.getPort(), entity.getBindAddress(), getListenerName(entity, "tcp-listener-"));
        }
        if (entity instanceof WebsocketListenerEntity) {
            final WebsocketListenerEntity wsEntity = (WebsocketListenerEntity) entity;
            return new WebsocketListener.Builder().allowExtensions(wsEntity.isAllowExtensions())
                    .bindAddress(wsEntity.getBindAddress())
                    .path(wsEntity.getPath())
                    .port(wsEntity.getPort())
                    .subprotocols(wsEntity.getSubprotocols())
                    .name(getListenerName(wsEntity, "websocket-listener-"))
                    .build();
        }
        if (entity instanceof TlsTCPListenerEntity) {
            final TlsTCPListenerEntity tlsEntity = (TlsTCPListenerEntity) entity;
            return new TlsTcpListener(tlsEntity.getPort(),
                    tlsEntity.getBindAddress(),
                    convertTls(tlsEntity.getTls()),
                    getListenerName(tlsEntity, "tls-tcp-listener-"));
        }
        if (entity instanceof TlsWebsocketListenerEntity) {
            final TlsWebsocketListenerEntity tslWsEntity = (TlsWebsocketListenerEntity) entity;
            return new TlsWebsocketListener.Builder().port(tslWsEntity.getPort())
                    .bindAddress(tslWsEntity.getBindAddress())
                    .path(tslWsEntity.getPath())
                    .allowExtensions(tslWsEntity.isAllowExtensions())
                    .tls(convertTls(tslWsEntity.getTls()))
                    .subprotocols(tslWsEntity.getSubprotocols())
                    .name(getListenerName(tslWsEntity, "tls-websocket-listener-"))
                    .build();
        }
        throw new IllegalArgumentException("Unknown listener type: " + entity.getClass().getSimpleName());
    }

    private @NotNull String getListenerName(final @NotNull ListenerEntity entity, final @NotNull String prefix) {
        final String name = (entity.getName() == null || entity.getName().trim().isEmpty()) ?
                prefix + entity.getPort() :
                entity.getName();
        if (listenerNames.contains(name)) {
            int count = 1;
            String newName = name + "-" + count++;
            while (listenerNames.contains(newName)) {
                newName = name + "-" + count++;
            }
            log.warn("Name '{}' already in use. Renaming listener with address '{}' and port '{}' to: '{}'",
                    name,
                    entity.getBindAddress(),
                    entity.getPort(),
                    newName);
            listenerNames.add(newName);
            return newName;
        } else {
            listenerNames.add(name);
            return name;
        }
    }
}
