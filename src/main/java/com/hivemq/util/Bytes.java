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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;


public final class Bytes {

    public static byte @Nullable [] toBytes(final @NotNull Optional<ByteBuffer> buf) {
        return Objects.requireNonNull(buf).map(Bytes::toBytes).orElse(null);
    }

    public static byte @Nullable [] toBytes(final @Nullable ByteBuffer buf) {
        if (buf != null) {
            final ByteBuffer rewind = buf.asReadOnlyBuffer().rewind();
            final byte[] bytes = new byte[rewind.remaining()];
            rewind.get(bytes);
            return bytes;
        }
        return null;
    }

    public static long readLong(final byte @NotNull [] bytes, int start) {
        if (start + Long.BYTES <= bytes.length) {
            return (bytes[start++] & 0xFFL) << 56 |
                    (bytes[start++] & 0xFFL) << 48 |
                    (bytes[start++] & 0xFFL) << 40 |
                    (bytes[start++] & 0xFFL) << 32 |
                    (bytes[start++] & 0xFFL) << 24 |
                    (bytes[start++] & 0xFFL) << 16 |
                    (bytes[start++] & 0xFFL) << 8 |
                    (bytes[start] & 0xFFL);
        }
        throw new IllegalArgumentException();
    }

    public static int readInt(final byte[] bytes, int start) {
        if (start + Integer.BYTES <= bytes.length) {
            return bytes[start++] << 24 |
                    (bytes[start++] & 0xFF) << 16 |
                    (bytes[start++] & 0xFF) << 8 |
                    (bytes[start] & 0xFF);
        }
        throw new IllegalArgumentException();
    }

    public static int readUShort(final byte @NotNull [] bytes, final int start) {
        if (start + Short.BYTES <= bytes.length) {
            return (bytes[start] & 0xFF) << 8 | (bytes[start + 1] & 0xFF);
        }
        throw new IllegalArgumentException();
    }

    public static boolean isSet(final byte number, final int off) {
        check(off < 8 && off >= 0);
        return (number & (1 << off)) != 0;
    }

    public static byte set(final byte number, final int off, final boolean value) {
        check(off < 8 && off >= 0);
        return value ? (byte) (number | (1 << off)) : (byte) (number & ~(1 << off));
    }

    public static void copyIntToBytes(final int anInt, final byte[] bytes, final int prefix) {
        check(bytes != null && bytes.length >= prefix + Integer.BYTES && prefix >= 0);
        bytes[prefix] = (byte) (anInt >> 24);
        bytes[prefix + 1] = (byte) (anInt >> 16);
        bytes[prefix + 2] = (byte) (anInt >> 8);
        bytes[prefix + 3] = (byte) anInt;
    }

    public static void copyLongToBytes(final long aLong, final byte @NotNull [] bytes, final int prefix) {
        check(bytes.length >= prefix + Long.BYTES && prefix >= 0);
        bytes[prefix] = (byte) (aLong >> 56);
        bytes[prefix + 1] = (byte) (aLong >> 48);
        bytes[prefix + 2] = (byte) (aLong >> 40);
        bytes[prefix + 3] = (byte) (aLong >> 32);
        bytes[prefix + 4] = (byte) (aLong >> 24);
        bytes[prefix + 5] = (byte) (aLong >> 16);
        bytes[prefix + 6] = (byte) (aLong >> 8);
        bytes[prefix + 7] = (byte) aLong;
    }

    public static void copyUShortToBytes(final int uShort, final byte @NotNull [] bytes, final int prefix) {
        check(bytes.length >= Short.BYTES + prefix && prefix >= 0 && uShort >= 0 && uShort <= 65535);
        bytes[prefix] = (byte) (uShort >> 8);
        bytes[prefix + 1] = (byte) uShort;
    }

    private static void check(final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }
}
