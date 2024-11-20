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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Strings {
    public static final String[] EMPTY_STRING_ARRAY = {};
    public static final char[] EMPTY_CHAR_ARRAY = {};

    private Strings() {
        //This is a utility class, don't instantiate it!
    }

    public static String[] splitPreserveAllTokens(final String str, final String separatorChars) {
        return splitWorker(str, separatorChars, -1, true);
    }

    public static boolean containsNone(final CharSequence cs, final String invalidChars) {
        if (invalidChars == null) {
            return true;
        }
        return containsNone(cs, invalidChars.toCharArray());
    }

    public static boolean endsWith(final CharSequence str, final CharSequence suffix) {
        return endsWith(str, suffix, false);
    }

    private static boolean endsWith(final CharSequence str, final CharSequence suffix, final boolean ignoreCase) {
        if (str == null || suffix == null) {
            return str == suffix;
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        final int strOffset = str.length() - suffix.length();
        return regionMatches(str, ignoreCase, strOffset, suffix, 0, suffix.length());
    }

    public static boolean containsAny(final CharSequence cs, final CharSequence searchChars) {
        if (searchChars == null) {
            return false;
        }
        return containsAny(cs, toCharArray(searchChars));
    }

    public static char[] toCharArray(final CharSequence source) {
        final int len = source == null ? 0 : source.length();
        if (len == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        if (source instanceof String) {
            return ((String) source).toCharArray();
        }
        final char[] array = new char[len];
        for (int i = 0; i < len; i++) {
            array[i] = source.charAt(i);
        }
        return array;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean containsAny(final CharSequence cs, final char... searchChars) {
        if (cs == null || cs.length() == 0 || searchChars == null || Array.getLength(searchChars) == 0) {
            return false;
        }


        final int csLength = cs.length();
        final int searchLength = searchChars.length;
        final int csLast = csLength - 1;
        final int searchLast = searchLength - 1;
        for (int i = 0; i < csLength; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    if (!Character.isHighSurrogate(ch)) {
                        // ch is in the Basic Multilingual Plane
                        return true;
                    }
                    if (j == searchLast) {
                        // missing low surrogate, fine, like String.indexOf(String)
                        return true;
                    }
                    if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean regionMatches(
            final CharSequence cs,
            final boolean ignoreCase,
            final int thisStart,
            final CharSequence substring,
            final int start,
            final int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
        }
        int index1 = thisStart;
        int index2 = start;
        int tmpLen = length;

        // Extract these first so we detect NPEs the same as the java.lang.String version
        final int srcLen = cs.length() - thisStart;
        final int otherLen = substring.length() - start;

        // Check for invalid parameters
        if (thisStart < 0 || start < 0 || length < 0) {
            return false;
        }

        // Check that the regions are long enough
        if (srcLen < length || otherLen < length) {
            return false;
        }

        while (tmpLen-- > 0) {
            final char c1 = cs.charAt(index1++);
            final char c2 = substring.charAt(index2++);

            if (c1 == c2) {
                continue;
            }

            if (!ignoreCase) {
                return false;
            }

            // The real same check as in String.regionMatches():
            final char u1 = Character.toUpperCase(c1);
            final char u2 = Character.toUpperCase(c2);
            if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
                return false;
            }
        }

        return true;
    }


    public static boolean containsNone(final CharSequence cs, final char... searchChars) {
        if (cs == null || searchChars == null) {
            return true;
        }
        final int csLen = cs.length();
        final int csLast = csLen - 1;
        final int searchLen = searchChars.length;
        final int searchLast = searchLen - 1;
        for (int i = 0; i < csLen; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (!Character.isHighSurrogate(ch)) {
                        // ch is in the Basic Multilingual Plane
                        return false;
                    }
                    if (j == searchLast) {
                        // missing low surrogate, fine, like String.indexOf(String)
                        return false;
                    }
                    if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static String[] splitWorker(
            final String str,
            final String separatorChars,
            final int max,
            final boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<>();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            final char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(EMPTY_STRING_ARRAY);
    }


    public static String stripEnd(final @Nullable String str, final @Nullable String stripChars) {
        int end = str == null ? 0 : str.length();
        if (end == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /**
     * Returns the value of a prefixed UTF-8 String from a {@link io.netty.buffer.ByteBuf}
     * according to the MQTT spec. The UTF-8 String is prefixed with a 16-bit value which
     * indicates the actual length of the String.
     * <p>
     * <b>This method will read from the {@link io.netty.buffer.ByteBuf} and will change the reader
     * index!</b>
     *
     * @param buf the {@link io.netty.buffer.ByteBuf} to read from
     * @return The UTF-8 String or <code>null</code> if there aren't enough bytes to read. This can happen if this
     *         method
     *         can not read the prefixed size of the String or if there are less bytes to read available than
     *         indicated by the prefixed 16-bit length.
     * @throws java.lang.NullPointerException if the passed {@link io.netty.buffer.ByteBuf} is <code>null</code>
     */
    public static String getPrefixedString(final ByteBuf buf) {
        Objects.requireNonNull(buf);
        if (buf.readableBytes() < 2) {
            return null;
        }
        final int utf8StringLength = buf.readUnsignedShort();
        if (buf.readableBytes() < utf8StringLength) {
            return null;
        }
        return getPrefixedString(buf, utf8StringLength);
    }

    public static String getPrefixedString(final ByteBuf buf, final int utf8StringLength) {
        Objects.requireNonNull(buf);
        final String string = buf.toString(buf.readerIndex(), utf8StringLength, UTF_8);
        //The ByteBuf.toString method, doesn't move the read index, therefor we have to do this manually.
        buf.skipBytes(utf8StringLength);
        return string;
    }

    public static String getValidatedPrefixedString(
            @NotNull final ByteBuf buf,
            final int utf8StringLength,
            final boolean validateShouldNotCharacters) {
        Objects.requireNonNull(buf);
        if (buf.readableBytes() < utf8StringLength) {
            return null;
        }
        final byte[] bytes = new byte[utf8StringLength];
        buf.getBytes(buf.readerIndex(), bytes);
        if (Utf8Utils.containsMustNotCharacters(bytes)) {
            return null;
        }
        if (validateShouldNotCharacters && Utf8Utils.hasControlOrNonCharacter(bytes)) {
            return null;
        }
        //The ByteBuf.getBytes method, doesn't move the read index, therefor we have to do this manually.
        buf.skipBytes(utf8StringLength);
        return new String(bytes, UTF_8);
    }

    /**
     * Writes a String onto a {@link io.netty.buffer.ByteBuf}. This encodes the
     * String according to the MQTT spc. The string gets prefixed with a 16-bit value
     * which indicates the actual length of the string
     *
     * @param string the string to encode
     * @param buffer the byte buffer
     * @return the encoded string as {@link io.netty.buffer.ByteBuf}
     */
    public static ByteBuf createPrefixedBytesFromString(final String string, final ByteBuf buffer) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(buffer);
        if (Utf8Utils.stringIsOneByteCharsOnly(string)) {
            // In case ther is no character in the string that is encoded with more than one byte in UTF-8,
            // We can write the string character by character without copying it to a temporary byte array.
            buffer.writeShort(string.length());
            for (int i = 0; i < string.length(); i++) {
                buffer.writeByte(string.charAt(i));
            }
        } else {
            final byte[] bytes = string.getBytes(UTF_8);
            buffer.writeShort(bytes.length);
            buffer.writeBytes(bytes);
        }
        return buffer;
    }

    /**
     * <p>This method can be used to convert a long value into a human readable byte format</p>
     *
     * <p>1024 bytes = 1.00 KB</p>
     * <p>1024*1024 bytes = 1.00 MB</p>
     * <p>1024*1024*1024 bytes = 1.00 GB</p>
     * <p>1024*1024*1024*1024 bytes = 1.00 TB</p>
     *
     * @param bytes the long value to convert
     * @return the human readable converted String
     */
    public static String convertBytes(final long bytes) {
        final long kbDivisor = 1024L;
        final long mbDivisor = kbDivisor * kbDivisor;
        final long gbDivisor = mbDivisor * kbDivisor;
        final long tbDivisor = gbDivisor * kbDivisor;
        if (bytes <= kbDivisor) {
            return bytes + " B";
        } else if (bytes <= mbDivisor) {
            final double kb = (double) bytes / kbDivisor;
            return String.format(Locale.US, "%.2f", kb) + " KB";
        } else if (bytes <= gbDivisor) {
            final double mb = (double) bytes / mbDivisor;
            return String.format(Locale.US, "%.2f", mb) + " MB";
        } else if (bytes <= tbDivisor) {
            final double gb = (double) bytes / gbDivisor;
            return String.format(Locale.US, "%.2f", gb) + " GB";
        } else {
            final double tb = (double) bytes / tbDivisor;
            return String.format(Locale.US, "%.2f", tb) + " TB";
        }
    }

    public static boolean isBlank(final @Nullable CharSequence cs) {
        final int strLen = cs == null ? 0 : cs.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
