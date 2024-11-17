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
package com.hivemq.util;

import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import com.hivemq.configuration.info.SystemInformation;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.File;

@LazySingleton
public class LocalPersistenceFileUtil {

    private final @NotNull SystemInformation systemInformation;

    @Inject
    LocalPersistenceFileUtil(final @NotNull SystemInformation systemInformation) {
        this.systemInformation = systemInformation;
    }

    public synchronized @NotNull File getLocalPersistenceFolder() {
        return ensureExists(new File(systemInformation.getDataFolder(), "persistence"),
                "Could not create persistence folder");
    }

    public synchronized @NotNull File getVersionedLocalPersistenceFolder(
            final @NotNull String persistence, final @NotNull String version) {
        return ensureExists(new File(getLocalPersistenceFolder(), persistence + File.separator + version),
                "Could not create versioned persistence folder");
    }

    private synchronized @NotNull File ensureExists(final @NotNull File folder, final @NotNull String errorMsg) {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException(errorMsg + ": " + folder.getAbsolutePath());
        }
        return folder;
    }
}
