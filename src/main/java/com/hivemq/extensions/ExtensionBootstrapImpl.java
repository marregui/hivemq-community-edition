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

package com.hivemq.extensions;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.hivemq.ShutdownHooks;
import com.hivemq.configuration.info.SystemInformation;
import org.jetbrains.annotations.NotNull;
import com.hivemq.extensions.loader.ExtensionLifecycleHandler;
import com.hivemq.extensions.loader.ExtensionLoader;
import com.hivemq.extensions.services.auth.Authenticators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;


@Singleton
public class ExtensionBootstrapImpl implements ExtensionBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ExtensionBootstrapImpl.class);

    private final @NotNull ExtensionLoader extensionLoader;
    private final @NotNull ExtensionLifecycleHandler lifecycleHandler;
    private final @NotNull HiveMQExtensions hiveMQExtensions;
    private final @NotNull Authenticators authenticators;

    @Inject
    public ExtensionBootstrapImpl(
            final @NotNull ExtensionLoader extensionLoader,
            final @NotNull ExtensionLifecycleHandler lifecycleHandler,
            final @NotNull HiveMQExtensions hiveMQExtensions,
            final @NotNull Authenticators authenticators) {
        this.extensionLoader = extensionLoader;
        this.lifecycleHandler = lifecycleHandler;
        this.hiveMQExtensions = hiveMQExtensions;
        this.authenticators = authenticators;
    }

    @NotNull
    @Override
    public CompletableFuture<Void> startExtensionSystem() {
        log.info("Starting HiveMQ extension system.");

        ShutdownHooks.INSTANCE.add(new ExtensionSystemShutdownHook(this));
        final Path extensionFolder = SystemInformation.INSTANCE.getExtensionsFolder().toPath();

        // load already installed extensions
        final ImmutableCollection<HiveMQExtensionEvent> hiveMQExtensionEvents =
                extensionLoader.loadExtensions(extensionFolder, false);

        final ImmutableList.Builder<HiveMQExtensionEvent> extensionEventBuilder =
                ImmutableList.<HiveMQExtensionEvent>builder().addAll(hiveMQExtensionEvents);

        // start them if needed
        final ImmutableList<HiveMQExtensionEvent> allExtensions = extensionEventBuilder.build();
        return lifecycleHandler.handleExtensionEvents(allExtensions)
                .thenAccept(((v) -> authenticators.checkAuthenticationSafetyAndLifeness()));
    }

    @Override
    public void stopExtensionSystem() {
        final ImmutableList<HiveMQExtensionEvent> events = hiveMQExtensions.getEnabledHiveMQExtensions()
                .values()
                .stream()
                .map(extension -> new HiveMQExtensionEvent(HiveMQExtensionEvent.Change.DISABLE,
                        extension.getId(),
                        extension.getStartPriority(),
                        extension.getExtensionFolderPath(),
                        extension.isEmbedded()))
                .collect(ImmutableList.toImmutableList());

        // stop extensions
        lifecycleHandler.handleExtensionEvents(events).join();
        // not checking for authenticator safety
    }

    private static class ExtensionSystemShutdownHook implements ShutdownHooks.Hook {

        private static final Logger log = LoggerFactory.getLogger(ExtensionSystemShutdownHook.class);

        private final @NotNull ExtensionBootstrap extensionBootstrap;

        private ExtensionSystemShutdownHook(final @NotNull ExtensionBootstrap extensionBootstrap) {
            this.extensionBootstrap = extensionBootstrap;
        }

        @Override
        public @NotNull String name() {
            return "Extension System Shutdown Hook";
        }

        @Override
        public @NotNull ShutdownHooks.Priority priority() {
            return ShutdownHooks.Priority.LOW;
        }

        @Override
        public void run() {
            log.info("Shutting down extension system");
            try {
                extensionBootstrap.stopExtensionSystem();
            } catch (final Exception e) {
                log.error("Exception at Extension system shutdown", e);
            }
        }
    }
}
