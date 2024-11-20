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
package com.hivemq.config;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Singleton;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class HivemqId {

    private static final int ID_LENGTH = 5;
    private static final int ID_START = ' ';
    private static final int ID_END = 'z' + 1;
    private static final int GAP = ID_END - ID_START;

    private final @NotNull String hivemqId = random();

    private static String random() {
        final Random random = ThreadLocalRandom.current();
        final StringBuilder builder = new StringBuilder(ID_LENGTH);
        int count = ID_LENGTH;
        while (count-- != 0) {
            final int codePoint = random.nextInt(GAP) + ID_START;
            switch (Character.getType(codePoint)) {
                case Character.UNASSIGNED:
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                    count++;
                    continue;
            }
            final int len = Character.charCount(codePoint);
            if (count == 0 && len > 1) {
                count++;
                continue;
            }
            if (Character.isLetter(codePoint) || Character.isDigit(codePoint)) {
                builder.appendCodePoint(codePoint);
                if (len == 2) {
                    count--;
                }
            } else {
                count++;
            }
        }
        return builder.toString();
    }

    public @NotNull String get() {
        return hivemqId;
    }
}
