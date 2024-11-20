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

package util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertTrue;

public class TestExtensionUtil {
    public static @NotNull File createValidExtension(final @NotNull File extensions, final @NotNull String id)
            throws Exception {
        final File folder = new File(extensions, id);
        if (!folder.exists()) {
            assertTrue(folder.mkdirs());
        }
        Files.write(new File(folder, "hivemq-extension.xml").toPath(),
                ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                        "<hivemq-extension>" +
                        "<id>" +
                        id +
                        "</id>" +
                        "<name>Some Name</name>" +
                        "<version>1.2.3-Version</version>" +
                        "<priority>1000</priority>" +
                        "<start-priority>500</start-priority>" +
                        "</hivemq-extension>").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE);
        assertTrue(new File(folder, "extension.jar").createNewFile());
        return folder;
    }
}
