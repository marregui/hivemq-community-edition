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

import com.google.common.base.Throwables;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Message;
import com.hivemq.bootstrap.HiveMQNettyBootstrap;
import com.hivemq.bootstrap.ListenerStartupInformation;
import com.hivemq.configuration.info.SystemInformation;
import com.hivemq.configuration.service.ConfigurationService;
import com.hivemq.configuration.service.entity.Listener;
import com.hivemq.extensions.ExtensionBootstrap;
import com.hivemq.persistence.payload.PublishPayloadPersistence;
import com.hivemq.util.Checkpoints;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public final class HiveMQServer {

    private static final Logger log = LoggerFactory.getLogger(HiveMQServer.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            exitIfUnrecoverable(e);
            final boolean atCreation = e instanceof CreationException;
            if (atCreation || e instanceof ProvisionException) {
                exitIfUnrecoverable(e.getCause());
                for (final Message message : atCreation ?
                        ((CreationException) e).getErrorMessages() :
                        ((ProvisionException) e).getErrorMessages()) {
                    exitIfUnrecoverable(message.getCause());
                }
            }
            log.error("Problem: %s%n", Throwables.getRootCause(e));
        });
        System.setProperty("guice_include_stack_traces", "OFF");
    }

    private static void exitIfUnrecoverable(final @NotNull Throwable t) {
        if (t instanceof UnrecoverableException) {
            System.err.printf("unrecoverable: %s%n", t.getMessage());
            System.exit(1);
        }
    }

    public static void main(final String @NotNull [] args) throws Exception {
        Logging.initLogging(SystemInformation.INSTANCE.getConfigFolder());
        final IOC ioc = new IOC(new ConfigurationService());
        final Injector injector = ioc.init();

        // start
        final long start = System.nanoTime();
        injector.getInstance(PublishPayloadPersistence.class).init();
        injector.getInstance(ExtensionBootstrap.class).startExtensionSystem().get();
        // check start status
        final List<ListenerStartupInformation> startupInfo =
                Objects.requireNonNull(injector.getInstance(HiveMQNettyBootstrap.class).bootstrapServer().get());
        Checkpoints.checkpoint("listener-started");
        if (startupInfo.isEmpty()) {
            log.error("No listener was configured");
            throw new UnrecoverableException();
        }
        int success = 0;
        for (final ListenerStartupInformation info : startupInfo) {
            if (info.isSuccessful()) {
                final Listener listener = info.getListener();
                log.info("Started {} on address {} and on port {}.",
                        listener.readableName(),
                        listener.getBindAddress(),
                        listener.getPort());
                success++;
            } else {
                final Listener listener = info.getListener();
                log.error("Could not start {} on port {} and address {}. Is it already in use?",
                        listener.readableName(),
                        listener.getPort(),
                        listener.getBindAddress());
            }
        }
        if (success < 1) {
            log.error("Could not bind any listener. Stopping HiveMQ.");
            throw new UnrecoverableException();
        }
        System.gc();
        log.info("Started HiveMQ [{}] in {}ms",
                ioc.getHiveMQId(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }
}
