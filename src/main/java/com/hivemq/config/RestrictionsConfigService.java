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

import com.hivemq.codec.encoder.mqtt5.UnsignedDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class RestrictionsConfigService {

    public static final int UNLIMITED_CONNECTIONS = -1;
    public static final int UNLIMITED_BANDWIDTH = 0;
    public static final long MAX_CONNECTIONS_DEFAULT = UNLIMITED_CONNECTIONS;
    public static final int MAX_CLIENT_ID_LENGTH_DEFAULT = 65535;
    public static final long NO_CONNECT_IDLE_TIMEOUT_DEFAULT = 10000;
    public static final long INCOMING_BANDWIDTH_THROTTLING_DEFAULT = UNLIMITED_BANDWIDTH;
    public static final int MAX_TOPIC_LENGTH_DEFAULT = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    public static final long MAX_CONNECTIONS_MINIMUM = 0;
    public static final int MAX_CLIENT_ID_LENGTH_MINIMUM = 1;
    public static final int MAX_CLIENT_ID_LENGTH_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    public static final long NO_CONNECT_IDLE_TIMEOUT_MINIMUM = 1;
    public static final long INCOMING_BANDWIDTH_THROTTLING_MINIMUM = 0;
    public static final int MAX_TOPIC_LENGTH_MINIMUM = 1;
    public static final int MAX_TOPIC_LENGTH_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;

    private static final Logger log = LoggerFactory.getLogger(RestrictionsConfigService.class);

    private final AtomicLong maxConnections = new AtomicLong(MAX_CONNECTIONS_DEFAULT);
    private final AtomicInteger maxClientIdLength = new AtomicInteger(MAX_CLIENT_ID_LENGTH_DEFAULT);
    private final AtomicLong noConnectIdleTimeout = new AtomicLong(NO_CONNECT_IDLE_TIMEOUT_DEFAULT);
    private final AtomicLong incomingLimit = new AtomicLong(INCOMING_BANDWIDTH_THROTTLING_DEFAULT);
    private final AtomicInteger maxTopicLength = new AtomicInteger(MAX_TOPIC_LENGTH_DEFAULT);

    public long maxConnections() {
        return maxConnections.get();
    }

    public int maxClientIdLength() {
        return maxClientIdLength.get();
    }

    public long noConnectIdleTimeout() {
        return noConnectIdleTimeout.get();
    }

    public long incomingLimit() {
        return incomingLimit.get();
    }

    public int maxTopicLength() {
        return maxTopicLength.get();
    }

    public void setMaxConnections(final long maxConnections) {
        log.debug("Setting global maximum allowed connections to {}", maxConnections);
        this.maxConnections.set(maxConnections);
    }

    public void setMaxClientIdLength(final int maxClientIdLength) {
        log.debug("Setting the maximum client id length to {}", maxClientIdLength);
        this.maxClientIdLength.set(maxClientIdLength);
    }

    public void setNoConnectIdleTimeout(final long noConnectIdleTimeout) {
        log.debug(
                "Setting the timeout for disconnecting idle tcp connections before a connect message was received to {} milliseconds",
                noConnectIdleTimeout);
        this.noConnectIdleTimeout.set(noConnectIdleTimeout);
    }

    public void setIncomingLimit(final long incomingLimit) {
        log.debug("Throttling the global incoming traffic limit {} bytes/second", incomingLimit);
        this.incomingLimit.set(incomingLimit);
    }

    public void setMaxTopicLength(final int maxTopicLength) {
        log.debug("Setting the maximum topic length to {}", maxTopicLength);
        this.maxTopicLength.set(maxTopicLength);
    }
}
