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
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShutdownHooksTest {

    private @Nullable List<String> executions;
    private @NotNull ShutdownHooks shutdownHooks;

    @Before
    public void setUp() throws Exception {
        executions = new ArrayList<>();
        shutdownHooks = new ShutdownHooks();
    }

    @Test
    public void hookWhenAddedThenWillRun() {
        shutdownHooks.add(createShutdownHook("name", ShutdownHooks.Priority.LOW));
        assertEquals(1, shutdownHooks.getShutdownHooks().size());
        shutdownHooks.shutdown();
        assertEquals(1, executions.size());
    }

    @Test
    public void hookWhenRemovedThenWillNotRun() {
        final ShutdownHooks.Hook hook = createShutdownHook("name", ShutdownHooks.Priority.LOW);

        shutdownHooks.add(hook);
        assertEquals(1, shutdownHooks.getShutdownHooks().size());

        shutdownHooks.remove(hook);
        assertEquals(0, shutdownHooks.getShutdownHooks().size());
    }

    @Test
    public void hooksWhenHaveDifferentPriorityThenSortedByHighest() {
        final ShutdownHooks.Hook shutdownHook = createShutdownHook("hook1", ShutdownHooks.Priority.LOW);
        final ShutdownHooks.Hook shutdownHook2 = createShutdownHook("hook2", ShutdownHooks.Priority.FIRST);
        final ShutdownHooks.Hook shutdownHook3 = createShutdownHook("hook3", ShutdownHooks.Priority.HIGH);
        shutdownHooks.add(shutdownHook);
        shutdownHooks.add(shutdownHook2);
        shutdownHooks.add(shutdownHook3);

        assertEquals(3, shutdownHooks.getShutdownHooks().size());

        shutdownHooks.shutdown();

        assertEquals(3, executions.size());
        assertEquals("hook2", executions.get(0));
        assertEquals("hook3", executions.get(1));
        assertEquals("hook1", executions.get(2));
    }

    private @NotNull ShutdownHooks.Hook createShutdownHook(
            final @NotNull String name, final @NotNull ShutdownHooks.Priority priority) {

        return new ShutdownHooks.Hook() {
            @Override
            public @NotNull String name() {
                return name;
            }

            @Override
            public @NotNull ShutdownHooks.Priority priority() {
                return priority;
            }

            @Override
            public void run() {
                executions.add(name);
            }
        };
    }
}
