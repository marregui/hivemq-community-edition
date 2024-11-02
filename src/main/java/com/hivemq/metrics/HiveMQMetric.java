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
package com.hivemq.metrics;

import com.codahale.metrics.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class HiveMQMetric<T extends Metric> {

    private final @NotNull String name;
    private final @NotNull Class<T> clazz;


    private HiveMQMetric(final @NotNull String name, final @NotNull Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static <T extends Metric> @NotNull HiveMQMetric<T> valueOf(
            final @NotNull String name, final @NotNull Class<T> metricClass) {
        checkNotNull(name, "Name cannot be null");
        return new HiveMQMetric<>(name, metricClass);
    }

    public static @NotNull HiveMQMetric<Gauge> gaugeValue(final @NotNull String name) {
        checkNotNull(name, "Name cannot be null");
        return new HiveMQMetric<>(name, Gauge.class);
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull Class<? extends Metric> getClazz() {
        return clazz;
    }


    @FunctionalInterface
    public interface Gauge extends com.codahale.metrics.Gauge<Number> {
        @Override
        @Nullable Number getValue();
    }
}
