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
package com.hivemq.config.entity;

import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class WebsocketListener implements Listener {
    private int port;
    private final @NotNull String bindAddress;
    private final @NotNull String path;
    private final boolean allowExtensions;
    private final @NotNull List<String> subprotocols;
    private final @NotNull String name;

    protected WebsocketListener(
            final int port,
            final @NotNull String bindAddress,
            final @NotNull String path,
            final boolean allowExtensions,
            final @NotNull List<String> subprotocols,
            final @NotNull String name) {
        this.port = port;
        this.bindAddress = bindAddress;
        this.path = path;
        this.allowExtensions = allowExtensions;
        this.subprotocols = subprotocols;
        this.name = name;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public @NotNull String getBindAddress() {
        return bindAddress;
    }

    @Override
    public @NotNull String readableName() {
        return "Websocket Listener";
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getPath() {
        return path;
    }

    public boolean getAllowExtensions() {
        return allowExtensions;
    }

    public @NotNull List<String> getSubprotocols() {
        return subprotocols;
    }

    public static class Builder {

        protected @NotNull String path;
        protected @NotNull List<String> subprotocols;

        protected @Nullable String name;
        protected @Nullable Integer port;
        protected @Nullable String bindAddress;
        protected boolean allowExtensions;

        public Builder() {
            path = "";
            subprotocols = new ArrayList<>();
            subprotocols.add("mqtt"); //Add default subprotocol which is required by the MQTT spec
            allowExtensions = false;
        }

        public @NotNull Builder from(final @NotNull WebsocketListener websocketListener) {
            port = websocketListener.getPort();
            bindAddress = websocketListener.getBindAddress();
            path = websocketListener.getPath();
            name = websocketListener.getName();
            allowExtensions = websocketListener.getAllowExtensions();
            subprotocols = new ArrayList<>(websocketListener.getSubprotocols());
            return this;
        }

        public @NotNull Builder port(final int port) {
            this.port = port;
            return this;
        }

        public @NotNull Builder bindAddress(final @NotNull String bindAddress) {
            Objects.requireNonNull(bindAddress);
            this.bindAddress = bindAddress;
            return this;
        }

        public @NotNull Builder path(final @NotNull String path) {
            Objects.requireNonNull(path);
            this.path = path;
            return this;
        }

        public @NotNull Builder name(final @NotNull String name) {
            Objects.requireNonNull(name);
            this.name = name;
            return this;
        }

        public @NotNull Builder allowExtensions(final boolean allowExtensions) {
            this.allowExtensions = allowExtensions;
            return this;
        }

        public @NotNull Builder subprotocols(final @NotNull List<String> subprotocols) {
            Objects.requireNonNull(subprotocols);
            this.subprotocols = ImmutableList.copyOf(subprotocols);
            return this;
        }

        public @NotNull WebsocketListener build() throws IllegalStateException {
            if (port == null) {
                throw new IllegalStateException("The port for a Websocket listener was not set.");
            }
            if (bindAddress == null) {
                throw new IllegalStateException("The bind address for a Websocket listener was not set.");
            }
            if (name == null) {
                name = "websocket-listener-" + port;
            }
            return new WebsocketListener(port, bindAddress, path, allowExtensions, subprotocols, name);
        }
    }
}
