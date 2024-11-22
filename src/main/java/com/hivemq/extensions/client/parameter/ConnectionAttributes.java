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
package com.hivemq.extensions.client.parameter;


import com.google.common.collect.ImmutableMap;
import com.hivemq.bootstrap.Connection;
import com.hivemq.config.InternalConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.hivemq.extension.sdk.api.services.exception.LimitExceededException;
import io.netty.channel.Channel;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents connection attributes for a connected client.
 *
 * @author Silvio Giebl
 */

public class ConnectionAttributes {

    @Nullable
    private Map<String, ByteBuffer> data;
    private final int maxValueSizeBytes;

    /**
     * Retrieves the {@link ConnectionAttributes} associated with the given channel.
     *
     * @param channel the channel which the {@link ConnectionAttributes} are associated to
     * @return the {@link ConnectionAttributes} associated to the given channel if present.
     */
    @Nullable
    static ConnectionAttributes getInstanceIfPresent(@NotNull final Channel channel) {
        Objects.requireNonNull(channel, "Channel for connection attributes must not be null.");

        return Connection.of(channel).getConnectionAttributes();
    }

    /**
     * Retrieves the {@link ConnectionAttributes} associated with the given channel.
     * If no {@link ConnectionAttributes} are associated yet, a new object will be associated.
     *
     * @param channel the channel which the {@link ConnectionAttributes} are associated to
     * @return the {@link ConnectionAttributes} associated to the given channel.
     */
    @NotNull
    public static ConnectionAttributes getInstance(@NotNull final Channel channel) {
        Objects.requireNonNull(channel, "Channel for connection attributes must not be null.");

        final ConnectionAttributes connectionAttributes = getInstanceIfPresent(channel);
        if (connectionAttributes != null) {
            return connectionAttributes;
        }

        final int maxValueSizeBytes = InternalConfig.CONNECTION_ATTRIBUTE_STORE_MAX_VALUE_SIZE_BYTES;

        final Connection clientConnectionContext = Connection.of(channel);
        return clientConnectionContext.setConnectionAttributesIfAbsent(new ConnectionAttributes(maxValueSizeBytes));
    }

    
    ConnectionAttributes(final int maxValueSizeBytes) {
        this.maxValueSizeBytes = maxValueSizeBytes;
    }

    /**
     * Sets the given connection attribute for the connected client.
     *
     * @param key   the key the connection attribute
     * @param value the value of the connection attribute
     * @throws LimitExceededException when the size of the passed value exceeds the maximum allowed size in bytes
     */
    public synchronized void put(@NotNull final String key, @NotNull final ByteBuffer value) {
        Objects.requireNonNull(key, "Key of connection attribute must not be null.");
        Objects.requireNonNull(value, "Value of connection attribute must not be null.");

        if (value.remaining() > maxValueSizeBytes) {
            throw new LimitExceededException("value with a size of " +
                    value.remaining() +
                    " bytes for key '" +
                    key +
                    "' in connection attribute store is larger than the allowed limit of " +
                    maxValueSizeBytes +
                    " bytes");
        }

        if (data == null) {
            data = new HashMap<>(4);
        }
        data.put(key, value.duplicate().asReadOnlyBuffer());
    }

    /**
     * Retrieves the value of the connection attribute with the given key for the connected client.
     *
     * @param key the key the connection attribute
     * @return the value of the connection attribute with the given key if present, otherwise null
     */
    @NotNull
    public synchronized Optional<ByteBuffer> get(@NotNull final String key) {
        Objects.requireNonNull(key, "Key of connection attribute must not be null.");

        if (data == null) {
            return Optional.empty();
        }
        final ByteBuffer value = data.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.asReadOnlyBuffer());
    }

    /**
     * Retrieves all connection attributes for the connected client.
     *
     * @return all connection attributes as a map of key and value pairs
     */
    @NotNull
    public synchronized Optional<Map<String, ByteBuffer>> getAll() {
        if (data == null) {
            return Optional.empty();
        }
        final ImmutableMap.Builder<String, ByteBuffer> builder = new ImmutableMap.Builder<>();
        for (final Map.Entry<String, ByteBuffer> entry : data.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().asReadOnlyBuffer());
        }
        return Optional.of(builder.build());
    }

    /**
     * Removes the connection attribute with the given key for the connected client.
     *
     * @param key the key the connection attribute
     * @return the value of the removed connection attribute if it was present, otherwise null
     */
    @NotNull
    public synchronized Optional<ByteBuffer> remove(@NotNull final String key) {
        Objects.requireNonNull(key, "Key of connection attribute must not be null.");

        if (data == null) {
            return Optional.empty();
        }
        final ByteBuffer value = data.remove(key);
        if (data.isEmpty()) {
            data = null;
        }
        return Optional.ofNullable(value);
    }

    /**
     * Clears all connection attributes for the connected client.
     */
    public synchronized void clear() {
        data = null;
    }

}
