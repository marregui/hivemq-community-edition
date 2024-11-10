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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import util.ClearHiveMQPropertiesRule;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SystemInformationImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ClearHiveMQPropertiesRule clearHiveMQPropertiesRule = new ClearHiveMQPropertiesRule();

    private SystemInformation systemInformation;

    private String tempFolderPath;

    private static Map<String, String> getModifiableEnvironmentVariables() throws Exception {
        final Map<String, String> env = System.getenv();
        final Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        return (Map<String, String>) field.get(env);
    }

    private static void setEnvironmentVariable(final String key, final String value) throws Exception {
        getModifiableEnvironmentVariables().put(key, value);
    }

    private static void removeEnvironmentVariable(final String key) throws Exception {
        getModifiableEnvironmentVariables().remove(key);
    }

    @Before
    public void before() {
        tempFolderPath = tempFolder.getRoot().getAbsolutePath();
        System.setProperty(SystemInformation.HIVEMQ_HOME, tempFolderPath);
    }

    @Test
    public void test_getHiveMQHomeFolder() throws Exception {
        systemInformation = new SystemInformation();
        assertEquals(tempFolderPath, systemInformation.getHiveMQHomeFolder().getAbsolutePath());
    }

    @Test
    public void test_getHiveMQHomeFolder_from_system_information_with_path() throws Exception {
        systemInformation = new SystemInformation();
        assertEquals(tempFolderPath, systemInformation.getHiveMQHomeFolder().getAbsolutePath());
    }

    @Test
    public void test_getHiveMQHomeFolder_environmentVariable() throws Exception {
        final File testfolder = tempFolder.newFolder("home");
        System.getProperties().remove(SystemInformation.HIVEMQ_HOME);
        setEnvironmentVariable("HIVEMQ_HOME", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        removeEnvironmentVariable("HIVEMQ_HOME");
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getHiveMQHomeFolder().getAbsolutePath());
    }

    @Test
    public void test_getConfigFolder_default() throws Exception {
        systemInformation = new SystemInformation();
        assertEquals(tempFolderPath + File.separator + "conf", systemInformation.getConfigFolder().getAbsolutePath());
    }

    @Test
    public void test_getConfigFolder_property() throws Exception {
        final File testfolder = tempFolder.newFolder("testconfig");
        System.setProperty("hivemq.config.folder", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getConfigFolder().getAbsolutePath());
    }

    @Test
    public void test_getConfigFolder_environmentVariable() throws Exception {
        final File testfolder = tempFolder.newFolder("testconfig");
        setEnvironmentVariable("HIVEMQ_CONFIG_FOLDER", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        removeEnvironmentVariable("HIVEMQ_CONFIG_FOLDER");
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getConfigFolder().getAbsolutePath());
    }

    @Test
    public void test_getLogFolder_default() throws Exception {
        systemInformation = new SystemInformation();
        assertEquals(tempFolderPath + File.separator + "log", systemInformation.getLogFolder().getAbsolutePath());
    }

    @Test
    public void test_getLogFolder_property() throws Exception {
        final File testfolder = tempFolder.newFolder("testlogs");
        System.setProperty("hivemq.log.folder", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getLogFolder().getAbsolutePath());
    }

    @Test
    public void test_getLogFolder_environmentVariable() throws Exception {
        final File testfolder = tempFolder.newFolder("testlogs");
        setEnvironmentVariable("HIVEMQ_LOG_FOLDER", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        removeEnvironmentVariable("HIVEMQ_LOG_FOLDER");
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getLogFolder().getAbsolutePath());
    }

    @Test
    public void test_getDataFolder_default() throws Exception {
        systemInformation = new SystemInformation();
        assertEquals(tempFolderPath + File.separator + "data", systemInformation.getDataFolder().getAbsolutePath());
    }

    @Test
    public void test_getDataFolder_property() throws Exception {
        final File testfolder = tempFolder.newFolder("testdatas");
        System.setProperty("hivemq.data.folder", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getDataFolder().getAbsolutePath());
    }

    @Test
    public void test_getDataFolder_environmentVariable() throws Exception {
        final File testfolder = tempFolder.newFolder("testdatas");
        setEnvironmentVariable("HIVEMQ_DATA_FOLDER", testfolder.getAbsolutePath());
        systemInformation = new SystemInformation();
        removeEnvironmentVariable("HIVEMQ_DATA_FOLDER");
        assertEquals(testfolder.getAbsolutePath(), systemInformation.getDataFolder().getAbsolutePath());
    }

    @Test
    public void test_create_plugin_folder_if_not_exists() throws Exception {
        systemInformation = new SystemInformation();
        assertTrue(systemInformation.getExtensionsFolder().exists());
    }

    @Test
    public void test_create_data_folder_if_not_exists() throws Exception {
        systemInformation = new SystemInformation();
        assertTrue(systemInformation.getDataFolder().exists());
    }

    @Test
    public void test_create_log_folder_if_not_exists() throws Exception {
        systemInformation = new SystemInformation();
        assertTrue(systemInformation.getLogFolder().exists());
    }
}
