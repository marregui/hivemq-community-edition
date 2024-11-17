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

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.hivemq.bootstrap.SingletonModule;
import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LifecycleModule extends SingletonModule<Class<LifecycleModule>> {

    private static final Logger log = LoggerFactory.getLogger(LifecycleModule.class);

    private final @NotNull LifecycleRegistry registry;

    public LifecycleModule() {
        super(LifecycleModule.class);
        registry = new LifecycleRegistry();
    }

    private static <I> void hear(
            final @NotNull LifecycleRegistry registry,
            final @NotNull TypeEncounter<I> encounter,
            final @NotNull Class<? super I> rawType) {

        // recur up to Object.class
        if (rawType.getSuperclass() != null) {
            hear(registry, encounter, rawType.getSuperclass());
        }

        if (rawType.isAnnotationPresent(javax.inject.Singleton.class) ||
                rawType.isAnnotationPresent(com.google.inject.Singleton.class) ||
                rawType.isAnnotationPresent(LazySingleton.class)) {
            registry.addSingletonClass(rawType);
        }

        // post constructs
        Method pc = null;
        for (final Method method : rawType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (method.getParameterTypes().length != 0) {
                    throw new ProvisionException("@PostConstruct must not have parameters");
                }
                if (method.getExceptionTypes().length > 0) {
                    throw new ProvisionException("@PostConstruct must not throw checked exceptions");
                }
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new ProvisionException("@PostConstruct must not be static");
                }
                if (pc != null) {
                    throw new ProvisionException("More than one @PostConstruct for class " + rawType);
                }
                pc = method;
            }
        }
        if (pc != null && registry.canInvokePostConstruct(rawType)) {
            final Method finalPc = pc;
            encounter.register((InjectionListener<I>) listener -> {
                try {
                    finalPc.setAccessible(true);
                    finalPc.invoke(listener);
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
        pc = null;
        for (final Method method : rawType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if (method.getParameterTypes().length != 0) {
                    throw new ProvisionException("@PreDestroy must not have parameters");
                }
                if (pc != null) {
                    throw new ProvisionException("More than one @PreDestroy for class " + rawType);
                }
                pc = method;
            }
        }
        if (pc != null && registry.canInvokePreDestroy(rawType)) {
            final Method finalPc = pc;
            encounter.register((InjectionListener<I>) listener -> registry.addPreDestroyMethod(finalPc, listener));
        }
    }

    @Override
    protected void configure() {
        bind(LifecycleRegistry.class).toInstance(registry);
        bind(LifecycleShutdownRegistration.class).asEagerSingleton();
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final @NotNull TypeLiteral<I> type, final @NotNull TypeEncounter<I> encounter) {
                LifecycleModule.hear(registry, encounter, type.getRawType());
            }
        });
    }
}
