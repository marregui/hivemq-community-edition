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

import com.hivemq.bootstrap.SingletonModule;
import com.hivemq.configuration.service.InternalConfigurations;
import com.hivemq.configuration.service.RestrictionsConfigurationService;
import com.hivemq.util.ThreadFactoryUtil;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThrottlingModule extends SingletonModule<Class<ThrottlingModule>> {

    private static final long outLimit = InternalConfigurations.OUTGOING_BANDWIDTH_THROTTLING_DEFAULT_BYTES_PER_SEC;

    public ThrottlingModule() {
        super(ThrottlingModule.class);
    }

    @Override
    protected void configure() {
        bind(GlobalTrafficShapingHandler.class).toProvider(GlobalTrafficShapingProvider.class).in(Singleton.class);
    }


    private static class GlobalTrafficShapingProvider implements Provider<GlobalTrafficShapingHandler> {
        private static final Logger log = LoggerFactory.getLogger(GlobalTrafficShapingProvider.class);

        private final long inLimit;

        @Inject
        GlobalTrafficShapingProvider(final @NotNull RestrictionsConfigurationService config) {
            inLimit = config.incomingLimit();
            log.debug("Throttling incoming traffic to {} B/s", inLimit);
            log.debug("Throttling outgoing traffic to {} B/s", outLimit);
        }

        @Override
        public @NotNull GlobalTrafficShapingHandler get() {
            final ScheduledExecutorService executor =
                    Executors.newSingleThreadScheduledExecutor(ThreadFactoryUtil.create(
                            "global-traffic-shaper-executor-%d"));
            ShutdownHooks.INSTANCE.add(new ShutdownHooks.Hook() {
                @Override
                public @NotNull String name() {
                    return "Global Traffic Shaper Executor Shutdown Hook";
                }

                @Override
                public @NotNull ShutdownHooks.Priority priority() {
                    return ShutdownHooks.Priority.HIGH;
                }

                @Override
                public void run() {
                    executor.shutdownNow();
                }
            });
            return new GlobalTrafficShapingHandler(executor, outLimit, inLimit, 1000L);
        }
    }
}
