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

import com.hivemq.ShutdownHooks;
import org.jetbrains.annotations.NotNull;
import com.hivemq.metrics.jmx.JmxReporterBootstrap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MetricsShutdownHook implements ShutdownHooks.Hook {

    private final @NotNull JmxReporterBootstrap jmxReporterBootstrap;

    @Inject
    public MetricsShutdownHook(final @NotNull JmxReporterBootstrap jmxReporterBootstrap) {
        this.jmxReporterBootstrap = jmxReporterBootstrap;
    }

    @PostConstruct
    public void postConstruct() {
        ShutdownHooks.INSTANCE.add(this);
    }

    @Override
    public void run() {
        jmxReporterBootstrap.stop();
    }

    @Override
    public @NotNull String name() {
        return "Metrics Shutdown";
    }

    @Override
    public @NotNull ShutdownHooks.Priority priority() {
        return ShutdownHooks.Priority.LOW;
    }
}
