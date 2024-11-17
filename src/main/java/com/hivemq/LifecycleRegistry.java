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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.util.ThreadFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class LifecycleRegistry {

    private static final @NotNull Logger log = LoggerFactory.getLogger(LifecycleRegistry.class);

    private final @NotNull Map<String, Invoke> invokedStatus;
    private final @NotNull List<Invocable> preDestroy;
    private @Nullable ListeningExecutorService executor;

    LifecycleRegistry() {
        invokedStatus = new ConcurrentHashMap<>();
        preDestroy = Collections.synchronizedList(new ArrayList<>());
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    void addSingletonClass(final @NotNull Class<?> clazz) {
        invokedStatus.putIfAbsent(clazz.getCanonicalName(), new Invoke());
    }

    void addPreDestroyMethod(final @NotNull Method method, final @NotNull Object target) {
        preDestroy.add(new Invocable(Objects.requireNonNull(method), Objects.requireNonNull(target)));
    }

    <T> boolean canInvokePostConstruct(final @NotNull Class<T> clazz) {
        final Invoke invoke = invokedStatus.get(clazz.getCanonicalName());
        if (invoke == null) {
            return true;
        }
        final boolean was = invoke.construct;
        invoke.construct = true;
        return !was;
    }

    <T> boolean canInvokePreDestroy(final @NotNull Class<T> clazz) {
        final Invoke invoke = invokedStatus.get(clazz.getCanonicalName());
        if (invoke == null) {
            return true;
        }
        final boolean was = invoke.destroy;
        invoke.destroy = true;
        return !was;
    }

    public @NotNull ListenableFuture<?> executePreDestroy() {
        final ExecutorService executor = Executors.newFixedThreadPool(3, ThreadFactoryUtil.create("PreDestroy-%d"));
        this.executor = MoreExecutors.listeningDecorator(executor);
        final List<ListenableFuture<?>> futures = new ArrayList<>(preDestroy.size());
        for (final Invocable preDestroyInvokable : preDestroy) {
            futures.add(this.executor.submit(() -> {
                try {
                    preDestroyInvokable.method.invoke(preDestroyInvokable.target);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    log.error("Could not execute preDestroy method for class {}",
                            preDestroyInvokable.target.getClass(),
                            e);
                }
            }));
        }
        return Futures.allAsList(futures);
    }

    private static final class Invoke {
        private boolean construct;
        private boolean destroy;
    }

    private static final class Invocable {
        private final @NotNull Method method;
        private final @NotNull Object target;

        public Invocable(final @NotNull Method method, final @NotNull Object target) {
            this.method = method;
            this.target = target;
        }
    }
}
