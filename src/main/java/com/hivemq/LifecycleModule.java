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

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.hivemq.bootstrap.SingletonModule;
import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import com.hivemq.util.ThreadFactoryUtil;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LifecycleModule extends SingletonModule<Class<LifecycleModule>> {

    private static final Logger log = LoggerFactory.getLogger(LifecycleModule.class);

    private final @NotNull Map<String, InvokeStatus> invokeStatus;
    private final @NotNull List<Invocable> invocable;

    public LifecycleModule() {
        super(LifecycleModule.class);
        invokeStatus = new ConcurrentHashMap<>();
        invocable = Collections.synchronizedList(new ArrayList<>());
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
            invokeStatus.putIfAbsent(type.getCanonicalName(), new InvokeStatus());
        }
        for (final Method m : type.getDeclaredMethods()) {
            if (m.isAnnotationPresent(PostConstruct.class)) {
                if (m.getParameterTypes().length != 0 ||
                        m.getExceptionTypes().length > 0 ||
                        Modifier.isStatic(m.getModifiers())) {
                    throw new RuntimeException();
                }
                if (canInvokePostConstruct(type)) {
                    encounter.register(postConstructInvocation(m));
                }
                break;
            }
        }
        for (final Method m : type.getDeclaredMethods()) {
            if (m.isAnnotationPresent(PreDestroy.class)) {
                if (m.getParameterTypes().length != 0) {
                    throw new RuntimeException();
                }
                if (canInvokePreDestroy(type)) {
                    encounter.register(preDestroyInvocation(m));
                }
                break;
            }
        }
    }

    private <I> @NotNull InjectionListener<I> postConstructInvocation(final @NotNull Method method) {
        return target -> {
            try {
                method.setAccessible(true);
                method.invoke(target);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                if (e.getCause() instanceof UnrecoverableException) {
                    log.error("An unrecoverable Exception occurred. Exiting HiveMQ", e);
                    System.exit(1);
                }
                throw new RuntimeException(e);
            }
        };
    }

    private <I> @NotNull InjectionListener<I> preDestroyInvocation(final @NotNull Method method) {
        return target -> invocable.add(new Invocable(method, target));
    }

    @Override
    protected void configure() {
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final @NotNull TypeLiteral<I> type, final @NotNull TypeEncounter<I> encounter) {
                invoke(encounter, type.getRawType());
            }
        });
        ShutdownHooks.INSTANCE.add(new ShutdownHooks.Hook() {
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
                LifecycleModule.this.executePreDestroys();
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

    public void executePreDestroys() {
        final ExecutorService executor = Executors.newFixedThreadPool(3, ThreadFactoryUtil.create("PreDestroy-%d"));
        final List<Future<?>> futures = new ArrayList<>(invocable.size());
        for (final Invocable preDestroy : invocable) {
            futures.add(executor.submit(preDestroy::invoke));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Exceptions in lifecycle shutdown", e);
        }
    }

    private static final class InvokeStatus {
        private boolean postConstructCalled;
        private boolean preDestroyCalled;
    }

    private static final class Invocable {
        private final @NotNull Method method;
        private final @NotNull Object target;

        Invocable(final @NotNull Method method, final @NotNull Object target) {
            this.method = method;
            this.target = target;
        }

        void invoke() {
            try {
                method.invoke(target);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                log.error("Could not execute preDestroy method for class {}", target.getClass(), e);
            }
        }
    }
}
