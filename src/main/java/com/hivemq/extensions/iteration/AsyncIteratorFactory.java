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
package com.hivemq.extensions.iteration;

import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import com.hivemq.ShutdownHooks;
import org.jetbrains.annotations.NotNull;
import com.hivemq.util.ThreadFactoryUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@LazySingleton
public class AsyncIteratorFactory {

    private final @NotNull ExecutorService executorService;

    public AsyncIteratorFactory() {
        executorService = Executors.newFixedThreadPool(4, ThreadFactoryUtil.create("async-iterator-executor-%d"));
        ShutdownHooks.INSTANCE.add(new ShutdownHooks.Hook() {
            @Override
            public @NotNull String name() {
                return "Async Iterator Executor Shutdown";
            }

            @Override
            public @NotNull ShutdownHooks.Priority priority() {
                return ShutdownHooks.Priority.MEDIUM;
            }

            @Override
            public void run() {
                executorService.shutdown();
            }
        });
    }

    @NotNull
    public <V> AsyncIterator<V> createIterator(
            @NotNull final FetchCallback<V> fetchCallback,
            @NotNull final AsyncIterator.ItemCallback<V> iterationCallback) {

        return new AsyncLocalChunkIterator<V>(fetchCallback, iterationCallback, executorService);
    }

}
