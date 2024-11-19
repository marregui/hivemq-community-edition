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
package com.hivemq.persistence.local.xodus;

import com.hivemq.config.InternalConfig;
import org.jetbrains.annotations.NotNull;
import jetbrains.exodus.env.EnvironmentConfig;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.hivemq.config.InternalConfig.XODUS_LOG_CACHE_USE_NIO;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_DURABLE_WRITES_ENABLED;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_GC_DELETION_DELAY_MSEC;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_GC_FILES_INTERVAL;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_GC_MIN_AGE;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_GC_RUN_PERIOD_MSEC;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_JMX;
import static com.hivemq.config.InternalConfig.XODUS_PERSISTENCE_ENVIRONMENT_SYNC_PERIOD_MSEC;

@Singleton
public class EnvironmentUtil {

    /**
     * Creates a new Xodus Environment config from a PersistenceConfig
     *
     * @param name the name of the environmentConfig
     * @return a Xodus EnvironmentConfig
     * @throws NullPointerException if one of the parameters is <code>null</code>
     */
    public EnvironmentConfig createEnvironmentConfig(@NotNull final String name) {
        return createEnvironmentConfig(XODUS_PERSISTENCE_ENVIRONMENT_GC_MIN_AGE,
                XODUS_PERSISTENCE_ENVIRONMENT_GC_DELETION_DELAY_MSEC,
                XODUS_PERSISTENCE_ENVIRONMENT_GC_FILES_INTERVAL,
                XODUS_PERSISTENCE_ENVIRONMENT_GC_RUN_PERIOD_MSEC,
                XODUS_PERSISTENCE_ENVIRONMENT_SYNC_PERIOD_MSEC,
                XODUS_PERSISTENCE_ENVIRONMENT_DURABLE_WRITES_ENABLED,
                XODUS_PERSISTENCE_ENVIRONMENT_JMX,
                name);
    }

    /**
     * Creates a new Xodus Environment config from a PersistenceConfig
     *
     * @param gcMinAge        the gc file min age of persistence
     * @param gcDeletionDelay the gc files deletion delay of persistence in ms
     * @param gcFilesInterval the gc files interval of persistence in ms
     * @param gcRunPeriod     the gc run period of persistence in ms
     * @param syncPeriod      the sync period of persistence in ms
     * @param durableWrites   durable writes for persistence
     * @param jmxEnabled      jmx enabled for persistence
     * @param name            the name of the environmentConfig
     * @return a Xodus EnvironmentConfig
     * @throws NullPointerException if one of the parameters is <code>null</code>
     */
    public EnvironmentConfig createEnvironmentConfig(
            final int gcMinAge,
            final int gcDeletionDelay,
            final int gcFilesInterval,
            final int gcRunPeriod,
            final int syncPeriod,
            final boolean durableWrites,
            final boolean jmxEnabled,
            @NotNull final String name) {
        checkNotNull(name, "Name for environment config must not be null");
        final EnvironmentConfig env = new EnvironmentConfig();
        env.setGcFileMinAge(gcMinAge);
        env.setGcFilesDeletionDelay(gcDeletionDelay);
        env.setGcFilesInterval(gcFilesInterval);
        env.setGcRunPeriod(gcRunPeriod);
        env.setLogSyncPeriod(syncPeriod);
        env.setLogDurableWrite(durableWrites);
        env.setManagementEnabled(jmxEnabled);
        env.setLogCacheUseNio(XODUS_LOG_CACHE_USE_NIO);
        env.setMemoryUsagePercentage(InternalConfig.XODUS_PERSISTENCE_LOG_MEMORY_HEAP_PERCENTAGE);
        return env;
    }
}
