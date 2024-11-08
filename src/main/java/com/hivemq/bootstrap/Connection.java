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
package com.hivemq.bootstrap;

import com.google.common.util.concurrent.SettableFuture;
import com.hivemq.configuration.service.entity.Listener;
import com.hivemq.mqtt.handler.publish.PublishFlushHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.extension.sdk.api.client.parameter.ClientInformation;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionInformation;
import com.hivemq.extension.sdk.api.packets.auth.ModifiableDefaultPermissions;
import com.hivemq.extensions.client.ClientAuthenticators;
import com.hivemq.extensions.client.ClientAuthorizers;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.client.parameter.ConnectionAttributes;
import com.hivemq.extensions.events.client.parameters.ClientEventListeners;
import com.hivemq.mqtt.message.ProtocolVersion;
import com.hivemq.mqtt.message.connect.CONNECT;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.security.auth.SslClientCertificate;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

public abstract class Connection {

    public static final @NotNull AttributeKey<Connection> CHANNEL_ATTRIBUTE_NAME =
            AttributeKey.valueOf("ClientConnectionContext");

    protected final @NotNull Channel channel;
    protected final @NotNull PublishFlushHandler publishFlushHandler;
    protected final @NotNull Listener connectedListener;
    protected volatile @NotNull ClientState clientState = ClientState.CONNECTING;
    protected @Nullable ProtocolVersion protocolVersion;
    protected @Nullable String clientId;
    protected boolean cleanStart;
    protected @Nullable ModifiableDefaultPermissions authPermissions;
    protected @Nullable Integer clientReceiveMaximum;
    protected @Nullable Integer connectKeepAlive;
    protected @Nullable Long queueSizeMaximum;
    protected @Nullable Long clientSessionExpiryInterval;
    protected @Nullable Long connectReceivedTimestamp;
    protected @NotNull String @Nullable [] topicAliasMapping;
    protected boolean clientIdAssigned;
    protected boolean incomingPublishesSkipRest;
    protected boolean requestResponseInformation;
    protected @Nullable Boolean requestProblemInformation;
    protected @Nullable SettableFuture<Void> disconnectFuture;
    protected @Nullable ConnectionAttributes connectionAttributes;
    protected boolean sendWill = true;
    protected boolean preventLwt;
    protected @Nullable SslClientCertificate authCertificate;
    protected @Nullable String authSniHostname;
    protected @Nullable String authCipherSuite;
    protected @Nullable String authProtocol;
    protected @Nullable String authUsername;
    protected byte @Nullable [] authPassword;
    protected @Nullable CONNECT authConnect;
    protected @Nullable String authMethod;
    protected @Nullable ByteBuffer authData;
    protected @Nullable Mqtt5UserProperties authUserProperties;
    protected @Nullable ScheduledFuture<?> authFuture;
    protected @Nullable Long maxPacketSizeSend;
    protected @Nullable ClientContextImpl extensionClientContext;
    protected @Nullable ClientEventListeners extensionClientEventListeners;
    protected @Nullable ClientAuthenticators extensionClientAuthenticators;
    protected @Nullable ClientAuthorizers extensionClientAuthorizers;
    protected @Nullable ClientInformation extensionClientInformation;
    protected @Nullable ConnectionInformation extensionConnectionInformation;

    protected Connection(
            final @NotNull Channel channel,
            final @NotNull PublishFlushHandler publishFlushHandler,
            final @NotNull Listener connectedListener) {
        this.channel = channel;
        this.publishFlushHandler = publishFlushHandler;
        this.connectedListener = connectedListener;
    }

    public static @NotNull Connection of(final @NotNull Channel channel) {
        final Connection context = channel.attr(CHANNEL_ATTRIBUTE_NAME).get();
        if (context != null) {
            return context;
        }
        // This should never happen.
        throw new IllegalStateException("Channel has no ClientConnectionContext.");
    }

    public @NotNull Channel getChannel() {
        return channel;
    }

    public @NotNull PublishFlushHandler getPublishFlushHandler() {
        return publishFlushHandler;
    }

    public @NotNull ClientState getClientState() {
        return clientState;
    }

    public void proposeClientState(final @NotNull ClientState clientState) {
        if (!this.clientState.disconnected()) {
            this.clientState = clientState;
        }
    }

    public @Nullable ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(final @NotNull ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public @Nullable String getClientId() {
        return clientId;
    }

    public void setClientId(final @NotNull String clientId) {
        this.clientId = clientId;
    }

    public void setCleanStart(final boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    public @Nullable ModifiableDefaultPermissions getAuthPermissions() {
        return authPermissions;
    }

    public void setAuthPermissions(final @NotNull ModifiableDefaultPermissions authPermissions) {
        this.authPermissions = authPermissions;
    }

    public @NotNull Listener getConnectedListener() {
        return connectedListener;
    }

    public @Nullable Integer getClientReceiveMaximum() {
        return clientReceiveMaximum;
    }

    public void setClientReceiveMaximum(final @NotNull Integer clientReceiveMaximum) {
        this.clientReceiveMaximum = clientReceiveMaximum;
    }

    public @Nullable Integer getConnectKeepAlive() {
        return connectKeepAlive;
    }

    public void setConnectKeepAlive(final @NotNull Integer connectKeepAlive) {
        this.connectKeepAlive = connectKeepAlive;
    }

    public @Nullable Long getQueueSizeMaximum() {
        return queueSizeMaximum;
    }

    public void setQueueSizeMaximum(final @Nullable Long queueSizeMaximum) {
        this.queueSizeMaximum = queueSizeMaximum;
    }

    public @Nullable Long getClientSessionExpiryInterval() {
        return clientSessionExpiryInterval;
    }

    public void setClientSessionExpiryInterval(final @NotNull Long clientSessionExpiryInterval) {
        this.clientSessionExpiryInterval = clientSessionExpiryInterval;
    }

    public @Nullable Long getConnectReceivedTimestamp() {
        return connectReceivedTimestamp;
    }

    public void setConnectReceivedTimestamp(final @NotNull Long connectReceivedTimestamp) {
        this.connectReceivedTimestamp = connectReceivedTimestamp;
    }

    public @NotNull String @Nullable [] getTopicAliasMapping() {
        return topicAliasMapping;
    }

    public void setTopicAliasMapping(final @NotNull String @NotNull [] topicAliasMapping) {
        this.topicAliasMapping = topicAliasMapping;
    }

    public boolean isClientIdAssigned() {
        return clientIdAssigned;
    }

    public void setClientIdAssigned(final boolean clientIdAssigned) {
        this.clientIdAssigned = clientIdAssigned;
    }

    /**
     * True if this client is not allowed to publish any more messages, if false he is allowed to do so.
     */
    public boolean isIncomingPublishesSkipRest() {
        return incomingPublishesSkipRest;
    }

    public void setIncomingPublishesSkipRest(final boolean incomingPublishesSkipRest) {
        this.incomingPublishesSkipRest = incomingPublishesSkipRest;
    }

    public boolean isRequestResponseInformation() {
        return requestResponseInformation;
    }

    public void setRequestResponseInformation(final boolean requestResponseInformation) {
        this.requestResponseInformation = requestResponseInformation;
    }

    public @Nullable Boolean getRequestProblemInformation() {
        return requestProblemInformation;
    }

    public void setRequestProblemInformation(final boolean requestProblemInformation) {
        this.requestProblemInformation = requestProblemInformation;
    }

    /**
     * This future is added during connection and is set when the client disconnect handling is complete.
     */
    public @Nullable SettableFuture<Void> getDisconnectFuture() {
        return disconnectFuture;
    }

    public void setDisconnectFuture(final @NotNull SettableFuture<Void> disconnectFuture) {
        this.disconnectFuture = disconnectFuture;
    }

    /**
     * Attribute for storing connection attributes. It is added only when connection attributes are set.
     */
    public @Nullable ConnectionAttributes getConnectionAttributes() {
        return connectionAttributes;
    }

    public synchronized @NotNull ConnectionAttributes setConnectionAttributesIfAbsent(
            final @NotNull ConnectionAttributes connectionAttributes) {

        if (this.connectionAttributes == null) {
            this.connectionAttributes = connectionAttributes;
        }
        return this.connectionAttributes;
    }

    public boolean isSendWill() {
        return sendWill;
    }

    public void setSendWill(final boolean sendWill) {
        this.sendWill = sendWill;
    }

    public boolean isPreventLwt() {
        return preventLwt;
    }

    public void setPreventLwt(final boolean preventLwt) {
        this.preventLwt = preventLwt;
    }

    public @Nullable SslClientCertificate getAuthCertificate() {
        return authCertificate;
    }

    public void setAuthCertificate(final @NotNull SslClientCertificate authCertificate) {
        this.authCertificate = authCertificate;
    }

    public @Nullable String getAuthSniHostname() {
        return authSniHostname;
    }

    public void setAuthSniHostname(final @NotNull String authSniHostname) {
        this.authSniHostname = authSniHostname;
    }

    public @Nullable String getAuthCipherSuite() {
        return authCipherSuite;
    }

    public void setAuthCipherSuite(final @NotNull String authCipherSuite) {
        this.authCipherSuite = authCipherSuite;
    }

    public @Nullable String getAuthProtocol() {
        return authProtocol;
    }

    public void setAuthProtocol(final @NotNull String authProtocol) {
        this.authProtocol = authProtocol;
    }

    public @Nullable String getAuthUsername() {
        return authUsername;
    }

    public void setAuthUsername(final @NotNull String authUsername) {
        this.authUsername = authUsername;
    }

    public byte @Nullable [] getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(final byte @Nullable [] authPassword) {
        this.authPassword = authPassword;
    }

    public @Nullable CONNECT getAuthConnect() {
        return authConnect;
    }

    public void setAuthConnect(final @Nullable CONNECT authConnect) {
        this.authConnect = authConnect;
    }

    public @Nullable String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(final @NotNull String authMethod) {
        this.authMethod = authMethod;
    }

    public @Nullable ByteBuffer getAuthData() {
        return authData;
    }

    public void setAuthData(final @Nullable ByteBuffer authData) {
        this.authData = authData;
    }

    public @Nullable Mqtt5UserProperties getAuthUserProperties() {
        return authUserProperties;
    }

    public void setAuthUserProperties(final @Nullable Mqtt5UserProperties authUserProperties) {
        this.authUserProperties = authUserProperties;
    }

    public @Nullable ScheduledFuture<?> getAuthFuture() {
        return authFuture;
    }

    public void setAuthFuture(final @Nullable ScheduledFuture<?> authFuture) {
        this.authFuture = authFuture;
    }

    public @Nullable Long getMaxPacketSizeSend() {
        return maxPacketSizeSend;
    }

    public void setMaxPacketSizeSend(final @NotNull Long maxPacketSizeSend) {
        this.maxPacketSizeSend = maxPacketSizeSend;
    }

    public @Nullable ClientContextImpl getExtensionClientContext() {
        return extensionClientContext;
    }

    public void setExtensionClientContext(final @NotNull ClientContextImpl extensionClientContext) {
        this.extensionClientContext = extensionClientContext;
    }

    public @Nullable ClientEventListeners getExtensionClientEventListeners() {
        return extensionClientEventListeners;
    }

    public void setExtensionClientEventListeners(final @NotNull ClientEventListeners extensionClientEventListeners) {
        this.extensionClientEventListeners = extensionClientEventListeners;
    }

    public @Nullable ClientAuthenticators getExtensionClientAuthenticators() {
        return extensionClientAuthenticators;
    }

    public void setExtensionClientAuthenticators(final @NotNull ClientAuthenticators extensionClientAuthenticators) {
        this.extensionClientAuthenticators = extensionClientAuthenticators;
    }

    public @Nullable ClientAuthorizers getExtensionClientAuthorizers() {
        return extensionClientAuthorizers;
    }

    public void setExtensionClientAuthorizers(final @NotNull ClientAuthorizers extensionClientAuthorizers) {
        this.extensionClientAuthorizers = extensionClientAuthorizers;
    }

    public @Nullable ClientInformation getExtensionClientInformation() {
        return extensionClientInformation;
    }

    public void setExtensionClientInformation(final @NotNull ClientInformation extensionClientInformation) {
        this.extensionClientInformation = extensionClientInformation;
    }

    public @Nullable ConnectionInformation getExtensionConnectionInformation() {
        return extensionConnectionInformation;
    }

    public void setExtensionConnectionInformation(final @NotNull ConnectionInformation extensionConnectionInformation) {
        this.extensionConnectionInformation = extensionConnectionInformation;
    }

    public @NotNull Optional<String> getChannelIP() {
        final Optional<InetAddress> inetAddress = getChannelAddress();

        return inetAddress.map(InetAddress::getHostAddress);
    }

    public @NotNull Optional<InetAddress> getChannelAddress() {
        final Optional<SocketAddress> socketAddress = Optional.ofNullable(channel.remoteAddress());
        if (socketAddress.isPresent()) {
            final SocketAddress sockAddress = socketAddress.get();
            //If this is not an InetAddress, we're treating this as if there's no address
            if (sockAddress instanceof InetSocketAddress) {
                return Optional.ofNullable(((InetSocketAddress) sockAddress).getAddress());
            }
        }

        return Optional.empty();
    }
}
