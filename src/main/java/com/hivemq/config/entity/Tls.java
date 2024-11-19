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


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Tls {

    private final @NotNull String keystorePath;
    private final @NotNull String keystorePassword;
    private final @NotNull String keystoreType;
    private final @NotNull String privateKeyPassword;
    private final @Nullable String truststorePath;
    private final @Nullable String truststorePassword;
    private final @Nullable String truststoreType;
    private final int handshakeTimeout;
    private final @NotNull ClientAuthMode clientAuthMode;
    private final @NotNull List<String> protocols;
    private final @NotNull List<String> cipherSuites;
    private final @Nullable Boolean preferServerCipherSuites;

    protected Tls(
            final @NotNull String keystorePath,
            final @NotNull String keystorePassword,
            final @NotNull String keystoreType,
            final @NotNull String privateKeyPassword,
            final @Nullable String truststorePath,
            final @Nullable String truststorePassword,
            final @Nullable String truststoreType,
            final int handshakeTimeout,
            final @NotNull ClientAuthMode clientAuthMode,
            final @NotNull List<String> protocols,
            final @NotNull List<String> cipherSuites,
            final @Nullable Boolean preferServerCipherSuites) {
        Objects.requireNonNull(clientAuthMode, "clientAuthMode must not be null");
        Objects.requireNonNull(protocols, "protocols must not be null");
        Objects.requireNonNull(cipherSuites, "cipher suites must not be null");
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keystoreType = keystoreType;
        this.privateKeyPassword = privateKeyPassword;
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
        this.truststoreType = truststoreType;
        this.handshakeTimeout = handshakeTimeout;
        this.clientAuthMode = clientAuthMode;
        this.protocols = protocols;
        this.cipherSuites = cipherSuites;
        this.preferServerCipherSuites = preferServerCipherSuites;
    }

    public @NotNull String getKeystorePath() {
        return keystorePath;
    }

    public @NotNull String getKeystorePassword() {
        return keystorePassword;
    }

    public @NotNull String getKeystoreType() {
        return keystoreType;
    }

    public @NotNull String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public @Nullable String getTruststorePath() {
        return truststorePath;
    }

    public @Nullable String getTruststorePassword() {
        return truststorePassword;
    }

    public @Nullable String getTruststoreType() {
        return truststoreType;
    }

    public int getHandshakeTimeout() {
        return handshakeTimeout;
    }

    public @NotNull ClientAuthMode getClientAuthMode() {
        return clientAuthMode;
    }

    public @NotNull List<String> getProtocols() {
        return protocols;
    }

    public @NotNull List<String> getCipherSuites() {
        return cipherSuites;
    }

    public @Nullable Boolean isPreferServerCipherSuites() {
        return preferServerCipherSuites;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Tls tls = (Tls) o;
        if (!keystorePath.equals(tls.keystorePath)) {
            return false;
        }
        if (!keystorePassword.equals(tls.keystorePassword)) {
            return false;
        }
        if (!keystoreType.equals(tls.keystoreType)) {
            return false;
        }
        if (!privateKeyPassword.equals(tls.privateKeyPassword)) {
            return false;
        }
        if (!Objects.equals(truststorePath, tls.truststorePath)) {
            return false;
        }
        if (!Objects.equals(truststorePassword, tls.truststorePassword)) {
            return false;
        }
        if (!Objects.equals(truststoreType, tls.truststoreType)) {
            return false;
        }
        if (handshakeTimeout != tls.handshakeTimeout) {
            return false;
        }
        if (clientAuthMode != tls.clientAuthMode) {
            return false;
        }
        if (!protocols.equals(tls.protocols)) {
            return false;
        }
        if (!Objects.equals(preferServerCipherSuites, tls.preferServerCipherSuites)) {
            return false;
        }
        return cipherSuites.equals(tls.cipherSuites);
    }

    @Override
    public int hashCode() {
        int result = keystorePath.hashCode();
        result = 31 * result + keystorePassword.hashCode();
        result = 31 * result + keystoreType.hashCode();
        result = 31 * result + privateKeyPassword.hashCode();
        result = 31 * result + (truststorePath != null ? truststorePath.hashCode() : 0);
        result = 31 * result + (truststorePassword != null ? truststorePassword.hashCode() : 0);
        result = 31 * result + (truststoreType != null ? truststoreType.hashCode() : 0);
        result = 31 * result + handshakeTimeout;
        result = 31 * result + clientAuthMode.hashCode();
        result = 31 * result + protocols.hashCode();
        result = 31 * result + cipherSuites.hashCode();
        result = 31 * result + (preferServerCipherSuites != null ? preferServerCipherSuites.hashCode() : 0);
        return result;
    }

    public enum ClientAuthMode {
        NONE("none"), //Clients are not allowed to send X509 client certificates
        OPTIONAL("optional"), //Clients can send X509 client certificates but they're not required to do so
        REQUIRED("required"); //Clients must send X509 client certificates

        private final @NotNull String clientAuthMode;

        ClientAuthMode(final @NotNull String clientAuthMode) {
            this.clientAuthMode = clientAuthMode;
        }

        @Override
        public @NotNull String toString() {
            return clientAuthMode;
        }
    }

    public static class Builder {

        private @Nullable String keystorePath;
        private @Nullable String keystorePassword;
        private @Nullable String keystoreType;
        private @Nullable String privateKeyPassword;
        private @Nullable String truststorePath;
        private @Nullable String truststorePassword;
        private @Nullable String truststoreType;
        private int handshakeTimeout;
        private @Nullable ClientAuthMode clientAuthMode;
        private @Nullable List<String> protocols;
        private @Nullable List<String> cipherSuites;
        private @Nullable Boolean preferServerCipherSuites;

        public @NotNull Builder withKeystorePath(final @NotNull String keystorePath) {
            this.keystorePath = keystorePath;
            return this;
        }

        public @NotNull Builder withKeystorePassword(final @NotNull String keystorePassword) {
            this.keystorePassword = keystorePassword;
            return this;
        }

        public @NotNull Builder withKeystoreType(final @NotNull String keystoreType) {
            this.keystoreType = keystoreType;
            return this;
        }

        public @NotNull Builder withPrivateKeyPassword(final @NotNull String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
            return this;
        }

        public @NotNull Builder withTruststorePath(final @Nullable String truststorePath) {
            this.truststorePath = truststorePath;
            return this;
        }

        public @NotNull Builder withTruststorePassword(final @Nullable String truststorePassword) {
            this.truststorePassword = truststorePassword;
            return this;
        }

        public @NotNull Builder withTruststoreType(final @Nullable String truststoreType) {
            this.truststoreType = truststoreType;
            return this;
        }

        public @NotNull Builder withHandshakeTimeout(final int handshakeTimeout) {
            this.handshakeTimeout = handshakeTimeout;
            return this;
        }

        public @NotNull Builder withClientAuthMode(final @NotNull ClientAuthMode clientAuthMode) {
            this.clientAuthMode = clientAuthMode;
            return this;
        }

        public @NotNull Builder withProtocols(final @NotNull List<String> protocols) {
            this.protocols = protocols;
            return this;
        }

        public @NotNull Builder withCipherSuites(final @NotNull List<String> cipherSuites) {
            this.cipherSuites = cipherSuites;
            return this;
        }

        public @NotNull Builder withPreferServerCipherSuites(final @Nullable Boolean preferServerCipherSuites) {
            this.preferServerCipherSuites = preferServerCipherSuites;
            return this;
        }

        public @NotNull Tls build() {
            Objects.requireNonNull(keystorePath, "keystorePath must not be null");
            Objects.requireNonNull(keystorePassword, "keystorePassword must not be null");
            Objects.requireNonNull(keystoreType, "keystoreType must not be null");
            Objects.requireNonNull(privateKeyPassword, "privateKeyPassword must not be null");
            Objects.requireNonNull(clientAuthMode, "clientAuthMode must not be null");
            Objects.requireNonNull(protocols, "protocols must not be null");
            Objects.requireNonNull(cipherSuites, "cipher suites must not be null");

            return new Tls(keystorePath,
                    keystorePassword,
                    keystoreType,
                    privateKeyPassword,
                    truststorePath,
                    truststorePassword,
                    truststoreType,
                    handshakeTimeout,
                    clientAuthMode,
                    protocols,
                    cipherSuites,
                    preferServerCipherSuites) {
            };
        }
    }
}
