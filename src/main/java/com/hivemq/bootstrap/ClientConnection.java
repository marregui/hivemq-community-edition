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

import com.hivemq.config.entity.Listener;
import com.hivemq.mqtt.handler.publish.PublishFlushHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.mqtt.message.pool.Ids;
import io.netty.channel.Channel;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;


public class ClientConnection extends Connection {

    private final @NotNull Ids ids;
    private @Nullable AtomicInteger inFlightMessageCount;
    private boolean noSharedSubscription;
    private boolean incomingPublishesDefaultFailedSkipRest;
    private boolean inFlightMessagesSent;

    
    protected ClientConnection(
            final @NotNull Channel channel,
            final @NotNull PublishFlushHandler publishFlushHandler,
            final @NotNull Listener connectedListener) {
        super(channel, publishFlushHandler, connectedListener);
        clientState = ClientState.CONNECTING;
        ids = new Ids();
    }

    private ClientConnection(final @NotNull UndefinedClientConnection context) {
        super(context.channel, context.publishFlushHandler, context.connectedListener);
        this.ids = new Ids();
        this.clientState = context.clientState;
        this.protocolVersion = context.protocolVersion;
        this.clientId = context.clientId;
        this.cleanStart = context.cleanStart;
        this.authPermissions = context.authPermissions;
        this.clientReceiveMaximum = context.clientReceiveMaximum;
        this.connectKeepAlive = context.connectKeepAlive;
        this.queueSizeMaximum = context.queueSizeMaximum;
        this.clientSessionExpiryInterval = context.clientSessionExpiryInterval;
        this.connectReceivedTimestamp = context.connectReceivedTimestamp;
        this.topicAliasMapping = context.topicAliasMapping;
        this.clientIdAssigned = context.clientIdAssigned;
        this.incomingPublishesSkipRest = context.incomingPublishesSkipRest;
        this.requestResponseInformation = context.requestResponseInformation;
        this.requestProblemInformation = context.requestProblemInformation;
        this.disconnectFuture = context.disconnectFuture;
        this.connectionAttributes = context.connectionAttributes;
        this.sendWill = context.sendWill;
        this.preventLwt = context.preventLwt;
        this.authCertificate = context.authCertificate;
        this.authSniHostname = context.authSniHostname;
        this.authCipherSuite = context.authCipherSuite;
        this.authProtocol = context.authProtocol;
        this.authUsername = context.authUsername;
        this.authPassword = context.authPassword;
        this.authConnect = context.authConnect;
        this.authMethod = context.authMethod;
        this.authData = context.authData;
        this.authUserProperties = context.authUserProperties;
        this.authFuture = context.authFuture;
        this.maxPacketSizeSend = context.maxPacketSizeSend;
        this.extensionClientContext = context.extensionClientContext;
        this.extensionClientEventListeners = context.extensionClientEventListeners;
        this.extensionClientAuthenticators = context.extensionClientAuthenticators;
        this.extensionClientAuthorizers = context.extensionClientAuthorizers;
        this.extensionClientInformation = context.extensionClientInformation;
        this.extensionConnectionInformation = context.extensionConnectionInformation;
    }

    public static @NotNull ClientConnection of(final @NotNull Channel channel) {
        final Connection clientConnectionContext = channel.attr(Connection.CHANNEL_ATTRIBUTE_NAME).get();
        checkArgument(clientConnectionContext instanceof ClientConnection);
        return (ClientConnection) clientConnectionContext;
    }

    public static @NotNull ClientConnection from(final @NotNull Connection clientConnectionContext) {
        checkArgument(clientConnectionContext instanceof UndefinedClientConnection);
        final UndefinedClientConnection context = (UndefinedClientConnection) clientConnectionContext;
        Objects.requireNonNull(context.clientId, "Client id must not be null.");
        Objects.requireNonNull(context.clientState, "Client state must not be null.");
        Objects.requireNonNull(context.protocolVersion, "Protocol version must not be null.");
        Objects.requireNonNull(context.connectedListener, "Connected listener must not be null.");
        final ClientConnection clientConnection = new ClientConnection(context);
        context.getChannel().attr(Connection.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);
        return clientConnection;
    }

    /**
     * The amount of messages that have been polled but not yet delivered.
     */
    public @Nullable AtomicInteger getInFlightMessageCount() {
        return inFlightMessageCount;
    }

    public void setInFlightMessageCount(final @Nullable AtomicInteger inFlightMessageCount) {
        this.inFlightMessageCount = inFlightMessageCount;
    }

    public @NotNull Ids getFreePacketIdRanges() {
        return ids;
    }

    /**
     * The amount of messages that have been polled but not yet delivered.
     */
    public int inFlightMessageCount() {
        if (inFlightMessageCount == null) {
            return 0;
        }
        return inFlightMessageCount.get();
    }

    public int decrementInFlightCount() {
        if (inFlightMessageCount == null) {
            return 0;
        }
        return inFlightMessageCount.decrementAndGet();
    }

    public int incrementInFlightCount() {
        if (inFlightMessageCount == null) {
            inFlightMessageCount = new AtomicInteger();
        }
        return inFlightMessageCount.incrementAndGet();
    }

    /**
     * True if it is guarantied that this client has no shared subscriptions, if false it is unclear.
     */
    public boolean getNoSharedSubscription() {
        return noSharedSubscription;
    }

    public void setNoSharedSubscription(final boolean noSharedSubscription) {
        this.noSharedSubscription = noSharedSubscription;
    }

    /**
     * True if this client is not allowed to publish any more messages by default, if false he is allowed to do so.
     */
    public boolean isIncomingPublishesDefaultFailedSkipRest() {
        return incomingPublishesDefaultFailedSkipRest;
    }

    public void setIncomingPublishesDefaultFailedSkipRest(final boolean incomingPublishesDefaultFailedSkipRest) {
        this.incomingPublishesDefaultFailedSkipRest = incomingPublishesDefaultFailedSkipRest;
    }

    /**
     * This reveres to the in-flight messages in the client queue, not the ones in the ordered topic queue.
     */
    public boolean isInFlightMessagesSent() {
        return inFlightMessagesSent;
    }

    public void setInFlightMessagesSent(final boolean inFlightMessagesSent) {
        this.inFlightMessagesSent = inFlightMessagesSent;
    }

    public boolean isMessagesInFlight() {
        return !inFlightMessagesSent || inFlightMessageCount() > 0;
    }

    public int getMaxInflightWindow(final int defaultMaxInflightWindow) {
        if (clientReceiveMaximum == null) {
            return defaultMaxInflightWindow;
        }
        return Math.min(clientReceiveMaximum, defaultMaxInflightWindow);
    }
}
