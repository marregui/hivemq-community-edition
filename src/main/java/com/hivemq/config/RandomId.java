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
public class RandomId {

    private final @NotNull String hivemqId = randomAlphanumeric(5);

    public static @NotNull String random(final int count) {
        return random(count, 0, 0, false, false);
    }

    public static byte @NotNull[] nextBytes(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        final byte[] result = new byte[size];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    public static String randomAscii(final int size) {
        return random(size, 32, 127, false, false);
    }

    public static String randomAlphanumeric(final int size) {
        return random(size, 0, 0, true, true);
    }

    public static String randomAlphabetic(final int size) {
        return random(size, 0, 0, true, false);
    }

    private static String random(final int size, int start, int end, final boolean letters, final boolean numbers) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        if (size == 0) {
            return "";
        }
        if (start == 0 && end == 0) {
            if (!letters && !numbers) {
                end = Character.MAX_CODE_POINT;
            } else {
                end = 'z' + 1;
                start = ' ';
            }
        } else if (end <= start) {
            throw new IllegalArgumentException();
        }
        if (numbers && end <= 48 || letters && end <= 65) {
            throw new IllegalArgumentException();
        }
        final Random random = ThreadLocalRandom.current();
        final StringBuilder sb = new StringBuilder(size);
        final int gap = end - start;
        int count = size;
        while (count-- != 0) {
            final int codePoint = random.nextInt(gap) + start;
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
            if (letters && Character.isLetter(codePoint) ||
                    numbers && Character.isDigit(codePoint) ||
                    !letters && !numbers) {
                sb.appendCodePoint(codePoint);
                if (len == 2) {
                    count--;
                }

            } else {
                count++;
            }
        }
        return sb.toString();
    }

    public @NotNull String get() {
        return hivemqId;
    }
}
