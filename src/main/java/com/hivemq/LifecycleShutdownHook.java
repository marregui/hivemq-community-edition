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
package com.hivemq;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LifecycleShutdownHook implements ShutdownHooks.Hook {

    private static final Logger log = LoggerFactory.getLogger(LifecycleShutdownHook.class);

    private final @NotNull LifecycleRegistry lifecycleRegistry;

    @Inject
    LifecycleShutdownHook(final @NotNull LifecycleRegistry lifecycleRegistry) {
        this.lifecycleRegistry = lifecycleRegistry;
    }

    @Override
    public @NotNull String name() {
        return "Lifecycle Shutdown";
    }

    @Override
    public @NotNull ShutdownHooks.Priority priority() {
        return ShutdownHooks.Priority.HIGH;
    }

    @Override
    public void run() {
        try {
            lifecycleRegistry.executePreDestroy().get(5, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Exceptions in lifecycle shutdown", e);
        }
    }
}
