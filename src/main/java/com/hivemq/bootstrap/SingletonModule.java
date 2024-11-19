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
package com.hivemq.bootstrap;

import com.google.inject.AbstractModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SingletonModule<T> extends AbstractModule {

    private static final @NotNull Logger log = LoggerFactory.getLogger(SingletonModule.class);

    private final @NotNull Class<T> key;

    public SingletonModule(final @NotNull Class<T> key) {
        this.key = key;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof SingletonModule && ((SingletonModule<?>) obj).key.equals(key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return getClass().getName() + "(key=" + key + ')';
    }

    public void instantiateOnStartup(final Class<?> clazz) {
        log.trace("Instantiating {} as eager singleton", clazz.getCanonicalName());
        bind(clazz).asEagerSingleton();
    }
}
