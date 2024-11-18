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

package com.hivemq.configuration.info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SystemInformation {

    public static final @NotNull SystemInformation INSTANCE = new SystemInformation();

    public static final @NotNull String HIVEMQ_HOME = "hivemq.home";
    public static final @NotNull String VERSION = "Origin";

    private final @NotNull File home;
    private final @NotNull File config;
    private final @NotNull File log;
    private final @NotNull File data;
    private final @NotNull File extensions;

    public SystemInformation()  {
        this.home = resolveHome();
        this.log = resolveFolder("hivemq.log.folder", "log");
        this.config = resolveFolder("hivemq.config.folder", "conf");
        this.data = resolveFolder("hivemq.data.folder", "data");
        this.extensions = resolveFolder("hivemq.extensions.folder", "extensions");
        System.setProperty("hivemq.log.folder", log.getAbsolutePath());
    }

    private static @Nullable String resolveProp(final @NotNull String sysProp) {
        String location = System.getProperty(sysProp);
        if (location == null) {
            location = System.getenv().get(sysProp.replaceAll("\\.", "_").toUpperCase());
        }
        return location;
    }

    private static @NotNull File resolveHome() {
        final File home;
        final String location = resolveProp(HIVEMQ_HOME);
        if (location != null) {
            home = new File(location).getAbsoluteFile();
        } else {
            try {
                home = Files.createTempDirectory("hivemq_home").toFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (home.exists() && home.isDirectory() && home.canRead() && home.canWrite()) {
            System.setProperty(HIVEMQ_HOME, home.getAbsolutePath());
            return home;
        }
        throw new IllegalStateException("HiveMQ home location is not set");
    }

    public @NotNull File getHiveMQHomeFolder() {
        return home;
    }

    public @NotNull File getConfigFolder() {
        return config;
    }

    public @NotNull File getLogFolder() {
        return log;
    }

    public @NotNull File getDataFolder() {
        return data;
    }

    public @NotNull File getExtensionsFolder() {
        return extensions;
    }

    private @NotNull File resolveFolder(final @NotNull String sysProp, final @NotNull String defaultName) {
        final String confName = resolveProp(sysProp);
        final File folder;
        if (confName != null) {
            final File tmp = new File(confName);
            folder = tmp.isAbsolute() ? tmp : new File(home, confName);
        } else {
            folder = new File(home, defaultName);
        }
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IllegalStateException("could not create folder: " + folder.getAbsolutePath());
            }
        }
        return folder;
    }
}
