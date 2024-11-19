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

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.bootstrap.lazysingleton.LazySingletonModule;
import com.hivemq.config.SysInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LocalPersistenceFileUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File systemFolder;

    @Before
    public void setUp() throws Exception {
        systemFolder = temporaryFolder.newFolder();
    }


    @Test
    public void test_is_singleton() {
        final Injector injector = Guice.createInjector(new LazySingletonModule());
        final LocalPersistenceFileUtil instance = injector.getInstance(LocalPersistenceFileUtil.class);
        final LocalPersistenceFileUtil instance2 = injector.getInstance(LocalPersistenceFileUtil.class);

        assertSame(instance, instance2);
    }


    @Test
    public void test_create_persistence_dir_if_it_doesnt_exist() throws Exception {

        final File dataFolder = temporaryFolder.newFolder();
        final SysInfo systemInfoForTest = createInfoForTest(dataFolder);
        final LocalPersistenceFileUtil util = new LocalPersistenceFileUtil(systemInfoForTest);


        final File localPersistenceFolder = util.getLocalPersistenceFolder();

        assertTrue(new File(dataFolder, "persistence").exists());

        assertEquals(new File(dataFolder, "persistence").getAbsolutePath(), localPersistenceFolder.getAbsolutePath());
    }

    @Test
    public void test_dont_overwrite_if_folder_already_exists() throws Exception {

        final File dataFolder = temporaryFolder.newFolder();
        final File persistenceFolder = new File(dataFolder, "persistence");
        assertTrue(persistenceFolder.mkdir());

        final File tempFile = new File(persistenceFolder, "testfile.tmp");
        Files.touch(tempFile);

        final SysInfo systemInfoForTest = createInfoForTest(dataFolder);
        final LocalPersistenceFileUtil util = new LocalPersistenceFileUtil(systemInfoForTest);


        util.getLocalPersistenceFolder();

        //File wasn't overwritten
        assertTrue(tempFile.exists());
    }

    @NotNull
    private SysInfo createInfoForTest(final File dataFolder) {
        return new SysInfo() {

            @NotNull
            @Override
            public File getHiveMQHomeFolder() {
                return systemFolder;
            }

            @NotNull
            @Override
            public File getConfigFolder() {
                return systemFolder;
            }

            @NotNull
            @Override
            public File getLogFolder() {
                return systemFolder;
            }

            @NotNull
            @Override
            public File getDataFolder() {
                return dataFolder;
            }

            @NotNull
            @Override
            public File getExtensionsFolder() {
                return systemFolder;
            }
        };
    }

}
