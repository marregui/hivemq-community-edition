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

package com.hivemq.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryUtil {

    private static final @NotNull ConcurrentHashMap<String, AtomicInteger> IDS = new ConcurrentHashMap<>();

    public static @NotNull ThreadFactory create(final @NotNull String nameFormat) {
        return r -> {
            final String threadName;
            final int wild = nameFormat.indexOf("%d");
            if (wild != -1) {
                final String name = nameFormat.substring(0, wild);
                threadName = name + IDS.computeIfAbsent(name, k -> new AtomicInteger()).incrementAndGet();
            } else {
                threadName = nameFormat;
            }
            final Thread thr = new Thread(r);
            thr.setName(threadName);
            thr.setDaemon(false);
            thr.setUncaughtExceptionHandler((thread, throwable) -> {
                System.err.printf("[%s] Uncaught exception: %s%n.", thread.getName(), throwable.getMessage());
                throwable.printStackTrace(System.err);
            });
            return thr;
        };
    }
}
