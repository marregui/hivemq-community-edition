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

import static com.google.common.base.Preconditions.checkNotNull;

public class TlsWebsocketListener extends WebsocketListener implements TlsListener {

    private final @NotNull Tls tls;

    private TlsWebsocketListener(
            final int port,
            final @NotNull String bindAddress,
            final @NotNull String path,
            final @NotNull Boolean allowExtensions,
            final @NotNull List<String> subprotocols,
            final @NotNull Tls tls,
            final @NotNull String name) {
        super(port, bindAddress, path, allowExtensions, subprotocols, name);
        this.tls = tls;
    }

    public @NotNull Tls getTls() {
        return tls;
    }

    @Override
    public @NotNull String readableName() {
        return "Websocket Listener with TLS";
    }

    public static class Builder {
        protected @NotNull String path;
        protected @NotNull List<String> subprotocols;
        protected @Nullable String name;
        protected @Nullable Integer port;
        protected @Nullable String bindAddress;
        protected boolean allowExtensions;

        private @Nullable Tls tls;

        public Builder() {
            path = "";
            subprotocols = new ArrayList<>();
            subprotocols.add("mqtt"); //Add default subprotocol which is required by the MQTT spec
            allowExtensions = false;
        }

        public @NotNull Builder from(final @NotNull TlsWebsocketListener tlsWebsocketListener) {
            port = tlsWebsocketListener.getPort();
            bindAddress = tlsWebsocketListener.getBindAddress();
            path = tlsWebsocketListener.getPath();
            name = tlsWebsocketListener.getName();
            allowExtensions = tlsWebsocketListener.getAllowExtensions();
            subprotocols = new ArrayList<>(tlsWebsocketListener.getSubprotocols());
            tls = tlsWebsocketListener.getTls();
            return this;
        }

        public @NotNull Builder tls(final @NotNull Tls tls) {
            checkNotNull(tls);
            this.tls = tls;
            return this;
        }

        public @NotNull Builder port(final int port) {
            this.port = port;
            return this;
        }

        public @NotNull Builder bindAddress(final @NotNull String bindAddress) {
            checkNotNull(bindAddress);
            this.bindAddress = bindAddress;
            return this;
        }

        public @NotNull Builder path(final @NotNull String path) {
            checkNotNull(path);
            this.path = path;
            return this;
        }

        public @NotNull Builder name(final @NotNull String name) {
            checkNotNull(name);
            this.name = name;
            return this;
        }

        public @NotNull Builder allowExtensions(final boolean allowExtensions) {
            this.allowExtensions = allowExtensions;
            return this;
        }

        public @NotNull Builder subprotocols(final @NotNull List<String> subprotocols) {
            checkNotNull(subprotocols);
            this.subprotocols = ImmutableList.copyOf(subprotocols);
            return this;
        }

        public @NotNull TlsWebsocketListener build() {
            if (port == null) {
                throw new IllegalStateException("The port for a TLS Websocket listener was not set.");
            }
            if (bindAddress == null) {
                throw new IllegalStateException("The bind address for a TLS Websocket listener was not set.");
            }
            if (name == null) {
                name = "tls-websocket-listener-" + port;
            }
            if (tls == null) {
                throw new IllegalStateException("The TLS settings for a TLS Websocket listener was not set.");
            }
            return new TlsWebsocketListener(port, bindAddress, path, allowExtensions, subprotocols, tls, name);
        }
    }
}
