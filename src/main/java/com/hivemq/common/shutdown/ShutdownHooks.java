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
package com.hivemq.common.shutdown;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownHooks {

    private static final @NotNull Logger log = LoggerFactory.getLogger(ShutdownHooks.class);
    private static final @NotNull ShutdownHooks INSTANCE = new ShutdownHooks();

    private final @NotNull AtomicBoolean hooksHaveRun = new AtomicBoolean();
    private final @NotNull PriorityQueue<Hook> hooks =
            new PriorityQueue<>(Comparator.comparingInt(Hook::priorityValue).reversed());

    public static void add(final @NotNull Hook shutdownHook) {
        INSTANCE.addHook(shutdownHook);
    }

    public static void remove(final @NotNull Hook shutdownHook) {
        INSTANCE.removeHook(shutdownHook);
    }

    public static void shutdown() {
        INSTANCE.runHooks();
    }

    public boolean hooksHaveRun() {
        return hooksHaveRun.get();
    }

    void addHook(final @NotNull ShutdownHooks.Hook shutdownHook) {
        Objects.requireNonNull(shutdownHook, "shutdownHook must not be null");
        if (!hooksHaveRun.get()) {
            log.trace("Adding shutdown hook {} with priority {}", shutdownHook.name(), shutdownHook.priority());
            synchronized (hooks) {
                hooks.add(shutdownHook);
            }
        }
    }

    void removeHook(final @NotNull ShutdownHooks.Hook shutdownHook) {
        Objects.requireNonNull(shutdownHook, "shutdownHook must not be null");
        if (!hooksHaveRun.get()) {
            log.trace("Removing shutdown hook {} with priority {}", shutdownHook.name(), shutdownHook.priority());
            synchronized (hooks) {
                hooks.remove(shutdownHook);
            }
        }
    }

    @VisibleForTesting
    public @NotNull PriorityQueue<Hook> getShutdownHooks() {
        return hooks;
    }

    void runHooks() {
        if (hooksHaveRun.compareAndSet(false, true)) {
            log.info("Shutting down HiveMQ. Please wait, this could take a while...");
            for (final Hook runnable : hooks) {
                log.trace(MarkerFactory.getMarker("SHUTDOWN_HOOK"), "Running shutdown hook {}", runnable.name());
                runnable.run();
            }
            log.info("Shutdown completed.");
        }
    }

    public enum Priority {
        FIRST(Integer.MAX_VALUE),
        HIGH(100_000),
        MEDIUM(50_000),
        LOW(Integer.MIN_VALUE);

        private final int value;
        private final @NotNull String str;

        Priority(final int value) {
            this.value = value;
            this.str = name() + " (" + value + ")";
        }

        @Override
        public @NotNull String toString() {
            return str;
        }
    }

    public interface Hook extends Runnable {

        @NotNull String name();

        default @NotNull Priority priority() {
            return Priority.LOW;
        }

        default int priorityValue() {
            return Priority.LOW.value;
        }
    }
}
