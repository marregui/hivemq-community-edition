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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * The Guice module which allows to use lifecycle annotations.
 * Lifecycle annotations which are supported at the moment are
 * <br>
 * * {@link javax.annotation.PostConstruct}
 * * {@link javax.annotation.PreDestroy}
 */
public class LifecycleModule extends SingletonModule<Class<LifecycleModule>> {

    private static final Logger log = LoggerFactory.getLogger(LifecycleModule.class);

    private final @NotNull LifecycleRegistry lifecycleRegistry;

    public LifecycleModule() {
        super(LifecycleModule.class);
        lifecycleRegistry = new LifecycleRegistry();
    }

    private static <I> boolean isSingleton(final @NotNull Class<? super I> rawType) {
        return rawType.isAnnotationPresent(javax.inject.Singleton.class) ||
                rawType.isAnnotationPresent(com.google.inject.Singleton.class) ||
                rawType.isAnnotationPresent(LazySingleton.class);
    }

    private static <I> @Nullable Method findPostConstruct(final @NotNull Class<? super I> rawType) {
        Method pc = null;
        for (final Method method : rawType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (method.getParameterTypes().length != 0) {
                    throw new ProvisionException("A method annotated with @PostConstruct must not have any parameters");
                }
                if (method.getExceptionTypes().length > 0) {
                    throw new ProvisionException(
                            "A method annotated with @PostConstruct must not throw any checked exceptions");
                }
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new ProvisionException("A method annotated with @PostConstruct must not be static");
                }
                if (pc != null) {
                    throw new ProvisionException("More than one @PostConstruct method found for class " + rawType);
                }
                pc = method;
            }
        }
        return pc;
    }

    private static <I> @Nullable Method findPreDestroy(final @NotNull Class<? super I> rawType) {
        Method pd = null;
        for (final Method method : rawType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if (method.getParameterTypes().length != 0) {
                    throw new ProvisionException("A method annotated with @PreDestroy must not have any parameters");
                }
                if (pd != null) {
                    throw new ProvisionException("More than one @PreDestroy method found for class " + rawType);
                }
                pd = method;
            }
        }
        return pd;
    }

    @Override
    protected void configure() {
        bind(LifecycleRegistry.class).toInstance(lifecycleRegistry);
        bind(LifecycleShutdownRegistration.class).asEagerSingleton();
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final @NotNull TypeLiteral<I> type, final @NotNull TypeEncounter<I> encounter) {
                executePostConstruct(encounter, type.getRawType());
            }
        });
    }

    private <I> void executePostConstruct(
            final @NotNull TypeEncounter<I> encounter, final @NotNull Class<? super I> rawType) {
        //We're going recursive up to every superclass until we hit Object.class
        if (rawType.getSuperclass() != null) {
            executePostConstruct(encounter, rawType.getSuperclass());
        }
        if (isSingleton(rawType)) {
            lifecycleRegistry.addSingletonClass(rawType);
        }
        final Method pc = findPostConstruct(rawType);
        if (pc != null) {
            if (lifecycleRegistry.canInvokePostConstruct(rawType)) {
                encounter.register((InjectionListener<I>) listener -> {
                    try {
                        pc.setAccessible(true);
                        pc.invoke(listener);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        if (e.getCause() instanceof UnrecoverableException) {
                            if (((UnrecoverableException) e.getCause()).isShowException()) {
                                log.error("An unrecoverable Exception occurred. Exiting HiveMQ", e);
                            }
                            System.exit(1);
                        }
                        throw new ProvisionException("An error occurred while calling @PostConstruct", e);
                    }
                });
            }
        }
        final Method pd = findPreDestroy(rawType);
        if (pd != null && lifecycleRegistry.canInvokePreDestroy(rawType)) {
            encounter.register((InjectionListener<I>) listener -> lifecycleRegistry.addPreDestroyMethod(pd, listener));
        }
    }
}
