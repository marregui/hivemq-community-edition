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
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.hivemq.bootstrap.SingletonModule;
import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import com.hivemq.util.ThreadFactoryUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LifecycleModule extends SingletonModule<Class<LifecycleModule>> {

    private static final Logger log = LoggerFactory.getLogger(LifecycleModule.class);

    private final @NotNull Map<String, InvokeStatus> invokeStatus;
    private final @NotNull List<Invocable> preDestroy;
    private @Nullable ListeningExecutorService executor;

    public LifecycleModule() {
        super(LifecycleModule.class);
        invokeStatus = new ConcurrentHashMap<>();
        preDestroy = Collections.synchronizedList(new ArrayList<>());
    }

    private <I> void invoke(
            final @NotNull TypeEncounter<I> encounter, final @NotNull Class<? super I> type) {
        // recur up to Object.class
        if (type.getSuperclass() != null) {
            invoke(encounter, type.getSuperclass());
        }

        if (type.isAnnotationPresent(javax.inject.Singleton.class) ||
                type.isAnnotationPresent(com.google.inject.Singleton.class) ||
                type.isAnnotationPresent(LazySingleton.class)) {
            addSingletonClass(type);
        }

        // post constructs
        Method method = null;
        for (final Method m : type.getDeclaredMethods()) {
            if (m.isAnnotationPresent(PostConstruct.class)) {
                if (m.getParameterTypes().length != 0) {
                    throw new ProvisionException("@PostConstruct must not have parameters");
                }
                if (m.getExceptionTypes().length > 0) {
                    throw new ProvisionException("@PostConstruct must not throw checked exceptions");
                }
                if (Modifier.isStatic(m.getModifiers())) {
                    throw new ProvisionException("@PostConstruct must not be static");
                }
                if (method != null) {
                    throw new ProvisionException("More than one @PostConstruct for class " + type);
                }
                method = m;
            }
        }
        if (method != null && canInvokePostConstruct(type)) {
            final Method postConstruct = method;
            encounter.register((InjectionListener<I>) listener -> {
                try {
                    postConstruct.setAccessible(true);
                    postConstruct.invoke(listener);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    if (e.getCause() instanceof UnrecoverableException) {
                        log.error("An unrecoverable Exception occurred. Exiting HiveMQ", e);
                        System.exit(1);
                    }
                    throw new ProvisionException("An error occurred while calling @PostConstruct", e);
                }
            });
        }

        // pre destroys
        method = null;
        for (final Method m : type.getDeclaredMethods()) {
            if (m.isAnnotationPresent(PreDestroy.class)) {
                if (m.getParameterTypes().length != 0) {
                    throw new ProvisionException("@PreDestroy must not have parameters");
                }
                if (method != null) {
                    throw new ProvisionException("More than one @PreDestroy for class " + type);
                }
                method = m;
            }
        }
        if (method != null && canInvokePreDestroy(type)) {
            final Method preDestroy = method;
            encounter.register((InjectionListener<I>) listener -> addPreDestroyMethod(preDestroy, listener));
        }
    }

    @Override
    protected void configure() {
        bind(LifecycleShutdownRegistration.class).asEagerSingleton();
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final @NotNull TypeLiteral<I> type, final @NotNull TypeEncounter<I> encounter) {
                invoke(encounter, type.getRawType());
            }
        });
    }

    <T> boolean canInvokePostConstruct(final @NotNull Class<T> clazz) {
        final InvokeStatus invoke = invokeStatus.get(clazz.getCanonicalName());
        if (invoke == null) {
            return true;
        }
        final boolean was = invoke.postConstructCalled;
        invoke.postConstructCalled = true;
        return !was;
    }

    <T> boolean canInvokePreDestroy(final @NotNull Class<T> clazz) {
        final InvokeStatus invoke = invokeStatus.get(clazz.getCanonicalName());
        if (invoke == null) {
            return true;
        }
        final boolean was = invoke.preDestroyCalled;
        invoke.preDestroyCalled = true;
        return !was;
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    void addSingletonClass(final @NotNull Class<?> clazz) {
        invokeStatus.putIfAbsent(clazz.getCanonicalName(), new InvokeStatus());
    }

    void addPreDestroyMethod(final @NotNull Method method, final @NotNull Object target) {
        preDestroy.add(new Invocable(Objects.requireNonNull(method), Objects.requireNonNull(target)));
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

    private static final class InvokeStatus {
        private boolean postConstructCalled;
        private boolean preDestroyCalled;
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
