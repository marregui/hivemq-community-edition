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
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class Strings {
    private static final @NotNull String @NotNull [] EMPTY_STR = {};
    private static final long kb = 1024L;
    private static final long mb = kb * kb;
    private static final long gb = mb * kb;
    private static final long tb = gb * kb;
    private static final byte BF = (byte) 0xBF;

    public static @Nullable String @NotNull [] splitOnFwdSlash(final @Nullable String str) {
        if (str != null) {
            final List<String> parts = new ArrayList<>(10);
            int start = 0;
            int i = 0;
            final int end = str.length();
            while (i < end) {
                if (str.charAt(i) == '/') {
                    parts.add(str.substring(start, i));
                    i++;
                    start = i;
                } else {
                    i++;
                }
            }
            parts.add(str.substring(start, i));
            return parts.toArray(EMPTY_STR);
        }
        return EMPTY_STR;
    }

    public static boolean containsNotWildcard(final @Nullable String str) {
        if (str != null) {
            for (int i = 0, n = str.length(); i < n; i++) {
                final char ch = str.charAt(i);
                if ('#' == ch || '+' == ch) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean endsWithSharp(final @Nullable String str) {
        return str != null && str.length() > 1 && str.regionMatches(false, str.length() - 2, "/#", 0, 2);
    }

    public static @Nullable String stripSlash(final @Nullable String str) {
        if (str != null) {
            int j = str.length();
            while (j > 0 && str.charAt(j - 1) == '/') {
                j--;
            }
            return j == str.length() ? str : str.substring(0, j);
        }
        return null;
    }

    public static String getPrefixedString(final @NotNull ByteBuf buf) {
        Objects.requireNonNull(buf);
        if (buf.readableBytes() < 2) {
            return null;
        }
        final int utf8Len = buf.readUnsignedShort();
        if (buf.readableBytes() < utf8Len) {
            return null;
        }
        final String str = buf.toString(buf.readerIndex(), utf8Len, UTF_8);
        buf.skipBytes(utf8Len);
        return str;
    }

    public static String getValidatedPrefixedString(
            final @NotNull ByteBuf buf,
            final int utf8Len,
            final boolean validateShouldNotCharacters) {
        Objects.requireNonNull(buf);
        if (buf.readableBytes() < utf8Len) {
            return null;
        }
        final byte[] bytes = new byte[utf8Len];
        buf.getBytes(buf.readerIndex(), bytes);
        if (containsMustNotCharacters(bytes)) {
            return null;
        }
        if (validateShouldNotCharacters && hasControlOrNonChars(bytes)) {
            return null;
        }
        buf.skipBytes(utf8Len);
        return new String(bytes, UTF_8);
    }

    public static ByteBuf createPrefixedBytesFromString(final @NotNull String str, final @NotNull ByteBuf buf) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(buf);
        if (stringIsOneByteCharsOnly(str)) {
            buf.writeShort(str.length());
            for (int i = 0; i < str.length(); i++) {
                buf.writeByte(str.charAt(i));
            }
        } else {
            final byte[] bytes = str.getBytes(UTF_8);
            buf.writeShort(bytes.length);
            buf.writeBytes(bytes);
        }
        return buf;
    }

    public static @NotNull String toHumanReadableSize(final long sizeBytes) {
        if (sizeBytes <= kb) {
            return sizeBytes + "B";
        } else if (sizeBytes <= mb) {
            return Math.ceil((double) sizeBytes / kb * 100) / 100 + "KB";
        } else if (sizeBytes <= gb) {
            return Math.ceil((double) sizeBytes / mb * 100) / 100 + "MB";
        } else if (sizeBytes <= tb) {
            return Math.ceil((double) sizeBytes / gb * 100) / 100 + "GB";
        } else {
            return Math.ceil((double) sizeBytes / tb * 100) / 100 + "TB";
        }
    }

    public static boolean isNotBlank(final @Nullable CharSequence cs) {
        final int strLen = cs == null ? 0 : cs.length();
        if (strLen == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean stringIsOneByteCharsOnly(final @NotNull String str) {
        for (int i = 0, len = str.length(); i < len; i++) {
            final char ch = str.charAt(i);
            if (ch > 0x7F) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsMustNotCharacters(final @NotNull String str) {
        boolean high = false;
        for (int i = 0, n = str.length(); i < n; i++) {
            final char ch = str.charAt(i);
            if (ch == 0) {
                return true;
            }
            if (high == !Character.isLowSurrogate(ch)) {
                return true;
            }
            high = Character.isHighSurrogate(ch);
        }
        return high;
    }

    public static boolean containsMustNotCharacters(final byte @NotNull [] bytes) {
        if (!isValidUtf8(bytes)) {
            return true;
        }
        for (final byte b : bytes) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidUtf8(final byte @NotNull [] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] < 0) {
                for (int j = i; ; ) {
                    int b1;
                    do {
                        if (j >= bytes.length) {
                            return true;
                        }
                    } while ((b1 = bytes[j++]) >= 0);
                    if (b1 < (byte) 0xE0) {
                        if (j == bytes.length) {
                            return false;
                        }
                        if (b1 < (byte) 0xC2 || bytes[j++] > BF) {
                            return false;
                        }
                    } else if (b1 < (byte) 0xF0) {
                        if (j + 1 >= bytes.length) {
                            return false;
                        }
                        final int b2 = bytes[j++];
                        if (b2 > BF ||
                                (b1 == (byte) 0xE0 && b2 < (byte) 0xA0) ||
                                (b1 == (byte) 0xED && (byte) 0xA0 <= b2) ||
                                bytes[j++] > BF) {
                            return false;
                        }
                    } else {
                        if (j + 2 >= bytes.length) {
                            return false;
                        }
                        final int b2 = bytes[j++];
                        if (b2 > BF ||
                                (((b1 << 28) + (b2 - (byte) 0x90)) >> 30) != 0 ||
                                bytes[j++] > BF ||
                                bytes[j++] > BF) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isValidUtf8(final @NotNull ByteBuf buf, final int utf8Len) {
        Objects.requireNonNull(buf);
        buf.markReaderIndex();
        try {
            final ByteBuf byteBuf = buf.slice(buf.readerIndex(), utf8Len);
            if (utf8Len < 0 || utf8Len > byteBuf.readableBytes()) {
                throw new IndexOutOfBoundsException();
            }
            for (int i = 0; i < utf8Len; i++) {
                if (byteBuf.readByte() < 0) {
                    for (int j = i; ; ) {
                        int b1 = 0;
                        do {
                            if (j >= utf8Len) {
                                return true;
                            }
                        } while (++j > 0 && (b1 = byteBuf.readByte()) >= 0);
                        if (b1 < (byte) 0xE0) {
                            if (j == utf8Len) {
                                return false;
                            }
                            if (b1 < (byte) 0xC2 || (++j > 0 && byteBuf.readByte() > BF)) {
                                return false;
                            }
                        } else if (b1 < (byte) 0xF0) {
                            if (j + 1 >= utf8Len) {
                                return false;
                            }
                            final int b2 = byteBuf.readByte();
                            j++;
                            if (b2 > BF ||
                                    (b1 == (byte) 0xE0 && b2 < (byte) 0xA0) ||
                                    (b1 == (byte) 0xED && (byte) 0xA0 <= b2) ||
                                    (++j > 0 && byteBuf.readByte() > BF)) {
                                return false;
                            }
                        } else {
                            if (j + 2 >= utf8Len) {
                                return false;
                            }
                            final int b2 = byteBuf.readByte();
                            j++;
                            if (b2 > BF ||
                                    (((b1 << 28) + (b2 - (byte) 0x90)) >> 30) != 0 ||
                                    (++j > 0 && byteBuf.readByte() > BF) ||
                                    (++j > 0 && byteBuf.readByte() > BF)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } finally {
            buf.resetReaderIndex();
        }
    }

    public static int utf8EncodedLen(final @NotNull String str) {
        int len = 0;
        for (int i = 0, n = str.length(); i < n; i++) {
            final char ch = str.charAt(i);
            if (ch <= 0x7F) {
                len++;
            } else if (ch <= 0x7FF) {
                len += 2;
            } else if (Character.isHighSurrogate(ch)) {
                len += 4;
                ++i;
            } else {
                len += 3;
            }
        }
        return len;
    }

    public static boolean hasControlOrNonChars(final byte @NotNull [] bytes) {
        Objects.requireNonNull(bytes);
        for (int i = 0; i < bytes.length; i++) {
            final byte b1 = bytes[i];
            if (b1 >= 1 && b1 <= 31 || b1 == (byte) 0x7F) {
                return true;
            }
            if (b1 > 31) {
                continue;
            }
            if (b1 < (byte) 0xE0) {
                if (i == bytes.length - 1) {
                    return false;
                }
                if (b1 == (byte) 0xC2 && bytes[i + 1] <= (byte) 0x9F) {
                    return true;
                }
            } else if (b1 < (byte) 0xF0) {
                if (i == bytes.length - 2) {
                    continue;
                }
                if (b1 == (byte) 0xEF &&
                        bytes[i + 1] == (byte) 0xB7 &&
                        (bytes[i + 2] >= (byte) 0x90 || bytes[i + 2] <= (byte) 0xAF)) {
                    return true;
                }
                if (b1 == (byte) 0xEF && bytes[i + 1] == BF && (bytes[i + 2] == (byte) 0xBE || bytes[i + 2] == BF)) {
                    return true;
                }
            } else {
                if (i == bytes.length - 3) {
                    continue;
                }
                if (b1 > (byte) 0xF4) {
                    continue;
                }
                final byte b2 = bytes[i + 1];
                final byte b3 = bytes[i + 2];
                final byte b4 = bytes[i + 3];
                if (!(b3 == BF && (b4 == (byte) 0xBE || b4 == BF))) {
                    continue;
                }
                if (b1 == (byte) 0xF0) {
                    if (b2 == (byte) 0x9F || b2 == (byte) 0xAF || b2 == BF) {
                        return true;
                    }
                } else {
                    if (b2 == (byte) 0x8F || b2 == (byte) 0x9F || b2 == (byte) 0xAF || b2 == BF) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasControlOrNonChars(final @NotNull String str) {
        Objects.requireNonNull(str);
        for (int i = 0, n = str.length(); i < n; i++) {
            final char ch = str.charAt(i);
            //control characters
            if (ch >= '\u0001' && ch <= '\u001F' || ch >= '\u007F' && ch <= '\u009F') {
                return true;
            }
            //non characters
            if (ch >= '\uFDD0' && ch <= '\uFDEF' || ch == '\uFFFE' || ch == '\uFFFF') {
                return true;
            }
            if (i == str.length() - 1) {
                return false;
            }
            final char next = str.charAt(i + 1);
            if (ch == '\uD83F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD87F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD8BF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD8FF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD93F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD97F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD9BF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uD9FF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDA3F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDA7F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDABF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDAFF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDB3F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDB7F' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDBBF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
            if (ch == '\uDBFF' && (next == '\uDFFE' || next == '\uDFFF')) {
                return true;
            }
        }
        return false;
    }
}
