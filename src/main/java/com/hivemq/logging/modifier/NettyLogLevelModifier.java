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
package com.hivemq.logging.modifier;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;


public class NettyLogLevelModifier implements LogLevelModifier {

    private static @NotNull FilterReply logUnlessUnsupportedOperationException(
            final @Nullable Marker marker,
            final @NotNull Logger logger,
            final @NotNull String format,
            final @Nullable Object @Nullable [] params,
            final @Nullable Throwable t) {
        if (t instanceof UnsupportedOperationException) {
            return FilterReply.DENY;
        }
        if (params != null) {
            for (final Object p : params) {
                if (p instanceof UnsupportedOperationException) {
                    return FilterReply.DENY;
                }
            }
        }
        logger.info(marker, format, params);
        return FilterReply.ACCEPT;
    }

    @Override
    public @NotNull FilterReply decide(
            final @Nullable Marker marker,
            final @NotNull Logger logger,
            final @NotNull Level level,
            final @NotNull String format,
            final @Nullable Object @Nullable [] params,
            final @Nullable Throwable t) {

        if (level == Level.DEBUG || level == Level.TRACE) {
            final String name = logger.getName();
            if (!name.startsWith("io.netty")) {
                return FilterReply.NEUTRAL;
            }
            if (level == Level.DEBUG) {
                if (name.startsWith("io.netty.handler.traffic.")) {
                    return FilterReply.DENY;
                }
                if (name.startsWith("io.netty.util.internal.NativeLibraryLoader")) {
                    return logUnlessUnsupportedOperationException(marker, logger, format, params, t);
                }
                return logUnlessUnsupportedOperationException(marker, logger, format, params, t);
            } else if (name.startsWith("io.netty.channel.nio.NioEventLoop")) {
                return logUnlessUnsupportedOperationException(marker, logger, format, params, t);
            }
        }
        return FilterReply.NEUTRAL;
    }
}
