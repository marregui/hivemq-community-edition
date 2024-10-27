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
package com.hivemq.diagnostic.data;


import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.time.LocalDateTime;

/**
 * @author Dominik Obermaier
 */
public class DiagnosticData {


    private static final String BANNER =
            "   __ ___          __  _______     ___  _                         __  _       \n" +
                    "  / // (_)  _____ /  |/  / __ \\   / _ \\(_)__ ____ ____  ___  ___ / /_(_)______\n" +
                    " / _  / / |/ / -_) /|_/ / /_/ /  / // / / _ `/ _ `/ _ \\/ _ \\(_-</ __/ / __(_-<\n" +
                    "/_//_/_/|___/\\__/_/  /_/\\___\\_\\ /____/_/\\_,_/\\_, /_//_/\\___/___/\\__/_/\\__/___/\n" +
                    "                                            /___/                             \n";

    private static final String SECTION_HEADLINE = "##############################\n";
    private final SystemPropertyInformation systemPropertyInformation;
    private final HiveMQInformation hiveMQInformation;
    private final HiveMQSystemInformation systemInformation;
    private final NetworkInterfaceInformation networkInterfaceInformation;


    @Inject
    DiagnosticData(
            final SystemPropertyInformation systemPropertyInformation,
            final HiveMQInformation hiveMQInformation,
            final HiveMQSystemInformation systemInformation,
            final NetworkInterfaceInformation networkInterfaceInformation) {
        this.systemPropertyInformation = systemPropertyInformation;
        this.hiveMQInformation = hiveMQInformation;
        this.systemInformation = systemInformation;
        this.networkInterfaceInformation = networkInterfaceInformation;
    }

    public String get() {

        final String diagnosticWriter = BANNER +
                "\n" +
                String.format("Generated at %s \n", LocalDateTime.now()) +
                "Please send this file along with any other files in the 'diagnostic' folder to support@hivemq.com.\n\n" +
                createHeadline("HiveMQ Information") +
                hiveMQInformation.getHiveMQInformation() +
                createHeadline("Java System Properties") +
                systemPropertyInformation.getSystemPropertyInformation() +
                createHeadline("System Information") +
                systemInformation.getSystemInformation() +
                createHeadline("Network Interfaces") +
                networkInterfaceInformation.getNetworkInterfaceInformation();

        return diagnosticWriter;
    }

    private String createHeadline(final String text) {
        final String builder = "\n" +
                SECTION_HEADLINE +
                "#" +
                StringUtils.center(text, SECTION_HEADLINE.length() - 3) +
                "#\n" +
                SECTION_HEADLINE +
                "\n";
        return builder;
    }
}
