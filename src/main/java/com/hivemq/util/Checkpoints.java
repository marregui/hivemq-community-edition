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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Checkpoints {

    private static final Logger log = LoggerFactory.getLogger(Checkpoints.class);

    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private static final ConcurrentHashMap<String, Integer> checkpointCounters = new ConcurrentHashMap<>(1);
    private static final ConcurrentHashMap<String, CountDownLatch> checkpointLatches = new ConcurrentHashMap<>(1);
    private static final ConcurrentHashMap<String, Runnable> checkPointCallbacks = new ConcurrentHashMap<>(1);
    private static boolean enabled;
    private static boolean debug;

    public static boolean enabled() {
        return enabled;
    }

    public static void checkpoint(final @NotNull String name) {
        if (!enabled) {
            return;
        }

        CountDownLatch latch = null;
        Runnable callback = null;

        final Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {

            if (debug) {
                log.error("Checkpoint {} visited", name);
            }

            final Integer previousValue = checkpointCounters.putIfAbsent(name, 1);
            if (previousValue != null) {
                checkpointCounters.put(name, previousValue + 1);
            }

            latch = checkpointLatches.get(name);
            callback = checkPointCallbacks.get(name);

        } finally {
            lock.unlock();
        }

        if (callback != null) {
            if (debug) {
                log.error("Found callback for checkpoint {}", name);
            }
            try {
                callback.run();
            } catch (final Throwable t) {
                log.error("ERROR at checkpoint callback", t);
            }
        }

        if (latch != null) {
            if (debug) {
                log.error("Found block latch for {}, blocking execution until latch is resumed", name);
            }
            try {
                latch.await();
            } catch (final InterruptedException e) {
                log.error("Checkpoint block interrupted", e);
            }
        }
    }

    public static void callbackOnCheckpoint(final @NotNull String name, final @NotNull Runnable callback) {
        if (!enabled) {
            return;
        }

        final Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            checkPointCallbacks.put(name, callback);
        } finally {
            lock.unlock();
        }
    }

  
}
