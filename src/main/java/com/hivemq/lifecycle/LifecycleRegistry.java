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
package com.hivemq.lifecycle;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class LifecycleRegistry {

    private static final Logger log = LoggerFactory.getLogger(LifecycleRegistry.class);

    private final @NotNull Map<String, Invoke> singletonInvokedStatus;
    private final @NotNull List<PreDestroyInvokable> preDestroy;

    private @Nullable ListeningExecutorService listeningExecutor;

    LifecycleRegistry() {
        singletonInvokedStatus = new ConcurrentHashMap<>();
        preDestroy = Collections.synchronizedList(new ArrayList<>());
    }

    public void shutdown() {
        if (listeningExecutor != null) {
            listeningExecutor.shutdown();
        }
    }

    public void addSingletonClass(final @NotNull Class<?> clazz) {
        singletonInvokedStatus.putIfAbsent(clazz.getCanonicalName(), new Invoke());
    }

    public void addPreDestroyMethod(final @NotNull Method method, final @NotNull Object target) {
        checkNotNull(method);
        checkNotNull(target);
        preDestroy.add(new PreDestroyInvokable(method, target));
    }

    public <T> boolean canInvokePostConstruct(final @NotNull Class<T> clazz) {
        final Invoke invoke = singletonInvokedStatus.get(clazz.getCanonicalName());
        if (invoke == null) {
            return true;
        }
        final boolean was = invoke.isPostConstructed();
        invoke.enablePostConstruct();
        return !was;
    }

    public <T> boolean canInvokePreDestroy(final @NotNull Class<T> clazz) {
        final Invoke invoke = singletonInvokedStatus.get(clazz.getCanonicalName());
        if (invoke == null) {
            return true;
        }
        final boolean was = invoke.isPreDestroyed();
        invoke.enablePreDestroy();
        return !was;
    }

    public @NotNull ListenableFuture<?> executePreDestroy() {
        final ExecutorService executor = Executors.newFixedThreadPool(3, ThreadFactoryUtil.create("PreDestroy-%d"));
        listeningExecutor = MoreExecutors.listeningDecorator(executor);
        final List<ListenableFuture<?>> futures = new ArrayList<>(preDestroy.size());
        for (final PreDestroyInvokable preDestroyInvokable : preDestroy) {
            futures.add(listeningExecutor.submit(() -> {
                try {
                    preDestroyInvokable.getMethod().invoke(preDestroyInvokable.getTarget());
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    log.error("Could not execute preDestroy method for class {}",
                            preDestroyInvokable.getTarget().getClass(),
                            e);
                }
            }));
        }
        return Futures.allAsList(futures);
    }

    private static final class Invoke {
        private boolean postConstruct;
        private boolean preDestroy;

        public boolean isPostConstructed() {
            return postConstruct;
        }

        public void enablePostConstruct() {
            postConstruct = true;
        }

        public boolean isPreDestroyed() {
            return preDestroy;
        }

        public void enablePreDestroy() {
            preDestroy = true;
        }
    }

    private static final class PreDestroyInvokable {
        private final @NotNull Method method;
        private final @NotNull Object target;

        public PreDestroyInvokable(final @NotNull Method method, final @NotNull Object target) {
            this.method = method;
            this.target = target;
        }

        public @NotNull Method getMethod() {
            return method;
        }

        public @NotNull Object getTarget() {
            return target;
        }
    }
}
