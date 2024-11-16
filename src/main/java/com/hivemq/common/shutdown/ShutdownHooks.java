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
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownHooks {

    public static final @NotNull ShutdownHooks INSTANCE = new ShutdownHooks();
    private static final @NotNull Logger log = LoggerFactory.getLogger(ShutdownHooks.class);
    private final @NotNull AtomicBoolean hooksHaveRun = new AtomicBoolean();
    private final @NotNull PriorityQueue<Hook> hooks = new PriorityQueue<>(Comparator.comparing(Hook::priority));

    public void add(final @NotNull Hook shutdownHook) {
        if (!hooksHaveRun.get()) {
            log.trace("Adding shutdown hook {} with priority {}", shutdownHook.name(), shutdownHook.priority());
            synchronized (hooks) {
                hooks.add(shutdownHook);
            }
        }
    }

    public void remove(final @NotNull Hook shutdownHook) {
        if (!hooksHaveRun.get()) {
            log.trace("Removing shutdown hook {} with priority {}", shutdownHook.name(), shutdownHook.priority());
            synchronized (hooks) {
                hooks.remove(shutdownHook);
            }
        }
    }

    public boolean hooksHaveRun() {
        return hooksHaveRun.get();
    }

    @VisibleForTesting
    public @NotNull PriorityQueue<Hook> getShutdownHooks() {
        synchronized (hooks) {
            return hooks;
        }
    }

    public void clear() {
        synchronized (hooks) {
            hooks.clear();
        }
    }

    public void shutdown() {
        if (hooksHaveRun.compareAndSet(false, true)) {
            log.info("Shutting down HiveMQ. Please wait, this could take a while...");
            do {
                final Hook hook = hooks.poll();
                if (hook != null) {
                    log.trace(MarkerFactory.getMarker("SHUTDOWN_HOOK"), "Running shutdown hook {}", hook.name());
                    try {
                        hook.run();
                    } catch (final Throwable t) {
                        log.error("Shutdown hook {} failed: {}", hook.name(), t.getMessage());
                    }
                }
            } while (!hooks.isEmpty());
            log.info("Shutdown completed.");
        }
    }

    public enum Priority {
        FIRST,
        HIGH,
        MEDIUM,
        LOW;

        private final @NotNull String str;

        Priority() {
            this.str = name() + " (" + ordinal() + ")";
        }

        @Override
        public @NotNull String toString() {
            return str;
        }
    }


    public interface Hook extends Runnable, Comparable<Hook> {

        @NotNull String name();

        @NotNull Priority priority();

        @Override
        default int compareTo(final @NotNull Hook that) {
            return priority().compareTo(that.priority());
        }
    }
}
