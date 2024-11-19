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
package com.hivemq.metrics.ioc.provider;

import com.codahale.metrics.MetricRegistry;
import com.hivemq.metrics.HiveMQMetrics;
import com.hivemq.metrics.gauges.OpenConnectionsGauge;
import io.netty.channel.group.ChannelGroup;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class OpenConnectionsGaugeProvider implements Provider<OpenConnectionsGauge> {

    private final @NotNull MetricRegistry metricRegistry;
    private final @NotNull ChannelGroup allChannels;

    @Inject
    public OpenConnectionsGaugeProvider(final @NotNull MetricRegistry metricRegistry, final @NotNull ChannelGroup allChannels) {
        this.metricRegistry = metricRegistry;
        this.allChannels = allChannels;
    }

    @Override
    public @NotNull OpenConnectionsGauge get() {
        final OpenConnectionsGauge connectionsGauge = new OpenConnectionsGauge(allChannels);
        metricRegistry.register(HiveMQMetrics.CONNECTIONS_OVERALL_CURRENT.name(), connectionsGauge);
        return connectionsGauge;
    }
}
