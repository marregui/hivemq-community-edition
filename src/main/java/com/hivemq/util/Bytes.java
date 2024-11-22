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
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;


public final class Bytes {

    public static boolean isBitSet(final byte number, final int off) {
        check(off < 8 && off >= 0);
        return (number & (1 << off)) != 0;
    }

    public static byte @NotNull [] toByteArray(final int value) {
        return new byte[]{
                (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }

    public static byte@NotNull[] toByteArray(long value) {
        final byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }

    private static void check(final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static byte setBit(final byte number, final int off, final boolean value) {
        return value ? setBit(number, off) : unsetBit(number, off);
    }

    public static byte setBit(final byte number, final int off) {
        check(off < 8 && off >= 0);
        return (byte) (number | (1 << off));
    }

    public static byte unsetBit(final byte number, final int off) {
        check(off < 8 && off >= 0);
        return (byte) (number & ~(1 << off));
    }

    public static byte[] getPrefixedBytes(final @NotNull ByteBuf buf) {
        if (buf.readableBytes() < 2) {
            return null;
        }
        final int len = buf.readUnsignedShort();
        if (buf.readableBytes() < len) {
            return null;
        }
        final byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    public static ByteBuf prefixBytes(final byte @NotNull [] bytes, final @NotNull ByteBuf buf) {
        Objects.requireNonNull(bytes);
        Objects.requireNonNull(buf);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
        return buf;
    }

    public static long readLong(final byte @NotNull [] bytes, final int start) {
        if (start + Long.BYTES <= bytes.length) {
            return fromBytes(bytes[start],
                    bytes[start + 1],
                    bytes[start + 2],
                    bytes[start + 3],
                    bytes[start + 4],
                    bytes[start + 5],
                    bytes[start + 6],
                    bytes[start + 7]);
        }
        throw new IllegalArgumentException();
    }

    public static int readInt(final byte[] bytes, final int start) {
        if (start + Integer.BYTES <= bytes.length) {
            return fromBytes(bytes[start], bytes[start + 1], bytes[start + 2], bytes[start + 3]);
        }
        throw new IllegalArgumentException();
    }

    public static int readUnsignedShort(final byte @NotNull [] bytes, final int start) {
        if (start + Short.BYTES <= bytes.length) {
            return (bytes[start] & 0xFF) << 8 | (bytes[start + 1] & 0xFF);
        }
        throw new IllegalArgumentException();
    }

    public static int fromBytes(final byte b1, final byte b2, final byte b3, final byte b4) {
        return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    public static long fromBytes(
            final byte b1,
            final byte b2,
            final byte b3,
            final byte b4,
            final byte b5,
            final byte b6,
            final byte b7,
            final byte b8) {
        return (b1 & 0xFFL) << 56 |
                (b2 & 0xFFL) << 48 |
                (b3 & 0xFFL) << 40 |
                (b4 & 0xFFL) << 32 |
                (b5 & 0xFFL) << 24 |
                (b6 & 0xFFL) << 16 |
                (b7 & 0xFFL) << 8 |
                (b8 & 0xFFL);
    }

    public static void copyIntToByteArray(final int anInt, final byte[] bytes, final int prefix) {
        check(bytes != null && bytes.length >= prefix + Integer.BYTES && prefix >= 0);
        bytes[prefix] = (byte) (anInt >> 24);
        bytes[prefix + 1] = (byte) (anInt >> 16);
        bytes[prefix + 2] = (byte) (anInt >> 8);
        bytes[prefix + 3] = (byte) anInt;
    }

    public static void copyLongToByteArray(final long aLong, final byte @NotNull [] bytes, final int prefix) {
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

    public static void copyUnsignedShortToByteArray(final int uShort, final byte @NotNull [] bytes, final int prefix) {
        check(bytes.length >= Short.BYTES + prefix && prefix >= 0 && uShort >= 0 && uShort <= 65535);
        bytes[prefix] = (byte) (uShort >> 8);
        bytes[prefix + 1] = (byte) uShort;
    }

    public static byte @Nullable [] getBytesFromReadOnlyBuffer(final @NotNull Optional<ByteBuffer> buf) {
        return Objects.requireNonNull(buf).map(Bytes::fromReadOnlyBuffer).orElse(null);
    }

    public static byte @Nullable [] fromReadOnlyBuffer(final @Nullable ByteBuffer buf) {
        if (buf != null) {
            final ByteBuffer rewind = buf.asReadOnlyBuffer().rewind();
            final byte[] bytes = new byte[rewind.remaining()];
            rewind.get(bytes);
            return bytes;
        }
        return null;
    }
}
