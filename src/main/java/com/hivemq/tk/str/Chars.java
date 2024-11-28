package com.hivemq.tk.str;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Chars {
    static final String[] CHAR_STRINGS;
    static final char[] base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    static final int[] base64Inverted = base64CreateInvertedAlphabet(base64);
    static final char[] base64Url = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
    static final int[] base64UrlInverted = base64CreateInvertedAlphabet(base64Url);

    static {
        CHAR_STRINGS = new String[128];
        for (char c = 0; c < 128; c++) {
            CHAR_STRINGS[c] = Character.toString(c);
        }
    }

    private Chars() {
    }

    public static void base64Decode(final @Nullable @NotNull CharSequence encoded, final @NotNull ByteBuffer target) {
        base64Decode(encoded, target, base64Inverted);
    }

    public static void base64Encode(@Nullable BinarySequence sequence, int maxLength, @NotNull CharSink<?> buffer) {
        int pad = base64Encode(sequence, maxLength, buffer, base64);
        for (int j = 0; j < pad; j++) {
            buffer.putAscii("=");
        }
    }

    public static void base64UrlDecode(@Nullable CharSequence encoded, @NotNull Utf8Sink target) {
        base64Decode(encoded, target, base64UrlInverted);
    }

    public static void base64UrlDecode(CharSequence encoded, ByteBuffer target) {
        base64Decode(encoded, target, base64UrlInverted);
    }

    public static void base64UrlEncode(BinarySequence sequence, int maxLength, CharSink<?> buffer) {
        base64Encode(sequence, maxLength, buffer, base64Url);
    }

    public static boolean contains(@NotNull CharSequence sequence, @NotNull CharSequence term) {
        return indexOf(sequence, 0, sequence.length(), term) != -1;
    }

    public static boolean endsWith(CharSequence cs, CharSequence ends) {
        if (ends == null || cs == null) {
            return false;
        }

        int l = ends.length();
        if (l == 0) {
            return true;
        }

        int csl = cs.length();
        return !(csl == 0 || csl < l) && equals(ends, cs, csl - l, csl);
    }

    public static boolean equals(@NotNull CharSequence l, @NotNull CharSequence r) {
        if (l == r) {
            return true;
        }

        int ll;
        if ((ll = l.length()) != r.length()) {
            return false;
        }

        return equalsChars(l, r, ll);
    }

    public static boolean equals(@NotNull CharSequence l, @NotNull CharSequence r, int rLo, int rHi) {
        if (l == r) {
            return true;
        }

        int ll = l.length();
        if (ll != rHi - rLo) {
            return false;
        }

        for (int i = 0; i < ll; i++) {
            if (l.charAt(i) != r.charAt(i + rLo)) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(@NotNull CharSequence l, char r) {
        return l.length() == 1 && l.charAt(0) == r;
    }

    public static boolean equalsIgnoreCase(@NotNull CharSequence l, @NotNull CharSequence r) {
        if (l == r) {
            return true;
        }

        int ll = l.length();
        if (ll != r.length()) {
            return false;
        }

        return equalsCharsIgnoreCase(l, r, ll);
    }

    public static boolean equalsLowerCaseAscii(@NotNull CharSequence l, @NotNull CharSequence r) {
        int ll = l.length();
        if (ll != r.length()) {
            return false;
        }

        for (int i = 0; i < ll; i++) {
            if (toLowerCaseAscii(l.charAt(i)) != toLowerCaseAscii(r.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static int hashCode(char @NotNull [] value, int lo, int hi) {
        if (hi == lo) {
            return 0;
        }

        int h = 0;
        for (int p = lo; p < hi; p++) {
            h = 31 * h + value[p];
        }
        return h;
    }

    public static int hashCode(@NotNull CharSequence value) {
        if (value instanceof String) {
            return value.hashCode();
        }

        int len = value.length();
        if (len == 0) {
            return 0;
        }

        int h = 0;
        for (int p = 0; p < len; p++) {
            h = 31 * h + value.charAt(p);
        }
        return h;
    }

    public static int indexOf(@NotNull CharSequence seq, int seqLo, int seqHi, @NotNull CharSequence term) {
        int termLen = term.length();
        if (termLen == 0) {
            return 0;
        }

        char first = term.charAt(0);
        int max = seqHi - termLen;

        for (int i = seqLo; i <= max; ++i) {
            if (seq.charAt(i) != first) {
                do {
                    ++i;
                } while (i <= max && seq.charAt(i) != first);
            }

            if (i <= max) {
                int j = i + 1;
                int end = j + termLen - 1;

                for (int k = 1; j < end && seq.charAt(j) == term.charAt(k); ++k) {
                    ++j;
                }

                if (j == end) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static int indexOf(
            @NotNull CharSequence seq,
            int seqLo,
            int seqHi,
            @NotNull CharSequence term,
            int occurrence) {
        int m = term.length();
        if (m == 0) {
            return 0;
        }

        if (occurrence == 0) {
            return -1;
        }

        int foundIndex = -1;
        int count = 0;
        if (occurrence > 0) {
            for (int i = seqLo; i < seqHi; i++) {
                if (foundIndex == -1) {
                    if (seqHi - i < m) {
                        return -1;
                    }
                    if (seq.charAt(i) == term.charAt(0)) {
                        foundIndex = i;
                    }
                } else { // first character matched, try to match the rest of the term
                    if (seq.charAt(i) != term.charAt(i - foundIndex)) {
                        // start again from after where the first character was found
                        i = foundIndex;
                        foundIndex = -1;
                    }
                }

                if (foundIndex != -1 && i - foundIndex == m - 1) {
                    count++;
                    if (count == occurrence) {
                        return foundIndex;
                    } else {
                        foundIndex = -1;
                    }
                }
            }
        } else { // if occurrence is negative, search in reverse
            for (int i = seqHi - 1; i >= seqLo; i--) {
                if (foundIndex == -1) {
                    if (i - seqLo + 1 < m) {
                        return -1;
                    }
                    if (seq.charAt(i) == term.charAt(m - 1)) {
                        foundIndex = i;
                    }
                } else { // last character matched, try to match the rest of the term
                    if (seq.charAt(i) != term.charAt(m - 1 + i - foundIndex)) {
                        // start again from after where the first character was found
                        i = foundIndex;
                        foundIndex = -1;
                    }
                }

                if (foundIndex != -1 && foundIndex - i == m - 1) {
                    count--;
                    if (count == occurrence) {
                        return foundIndex + 1 - m;
                    } else {
                        foundIndex = -1;
                    }
                }
            }
        }

        return -1;
    }

    public static int indexOf(CharSequence seq, final int seqLo, char c) {
        return indexOf(seq, seqLo, seq.length(), c);
    }

    public static int indexOf(CharSequence seq, int seqLo, int seqHi, char c) {
        return indexOf(seq, seqLo, seqHi, c, 1);
    }

    public static int indexOf(CharSequence seq, int seqLo, int seqHi, char ch, int occurrence) {
        if (occurrence == 0) {
            return -1;
        }

        int count = 0;
        if (occurrence > 0) {
            for (int i = seqLo; i < seqHi; i++) {
                if (seq.charAt(i) == ch) {
                    count++;
                    if (count == occurrence) {
                        return i;
                    }
                }
            }
        } else {    // if occurrence is negative, search in reverse
            for (int i = seqHi - 1; i >= seqLo; i--) {
                if (seq.charAt(i) == ch) {
                    count--;
                    if (count == occurrence) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    // Term has to be lower-case.
    public static int indexOfLowerCase(@NotNull CharSequence seq, int seqLo, int seqHi, @NotNull CharSequence termLC) {
        int termLen = termLC.length();
        if (termLen == 0) {
            return 0;
        }

        char first = termLC.charAt(0);
        int max = seqHi - termLen;

        for (int i = seqLo; i <= max; ++i) {
            if (Character.toLowerCase(seq.charAt(i)) != first) {
                do {
                    ++i;
                } while (i <= max && Character.toLowerCase(seq.charAt(i)) != first);
            }

            if (i <= max) {
                int j = i + 1;
                int end = j + termLen - 1;
                for (int k = 1; j < end && Character.toLowerCase(seq.charAt(j)) == termLC.charAt(k); ++k) {
                    ++j;
                }
                if (j == end) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static boolean isAscii(@NotNull CharSequence cs) {
        for (int i = 0, n = cs.length(); i < n; i++) {
            if (cs.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(CharSequence s) {
        if (s == null) {
            return true;
        }

        int len = s.length();
        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOnlyDecimals(CharSequence s) {
        int len = s.length();
        for (int i = len - 1; i > -1; i--) {
            int digit = s.charAt(i);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return len > 0;
    }

    public static boolean isQuote(char c) {
        switch (c) {
            case '\'':
            case '"':
            case '`':
                return true;
            default:
                return false;
        }
    }

    public static boolean isQuoted(CharSequence s) {
        if (s == null || s.length() < 2) {
            return false;
        }

        char open = s.charAt(0);
        return isQuote(open) && open == s.charAt(s.length() - 1);
    }

    public static int lastIndexOf(CharSequence sequence, int sequenceLo, int sequenceHi, CharSequence term) {
        return indexOf(sequence, sequenceLo, sequenceHi, term, -1);
    }

    public static int lowerCaseHashCode(CharSequence value) {
        int len = value.length();
        if (len == 0) {
            return 0;
        }

        int h = 0;
        for (int p = 0; p < len; p++) {
            h = 31 * h + Character.toLowerCase(value.charAt(p));
        }
        return h;
    }

    public static CharSequence repeat(String s, int times) {
        return new CharSequence() {
            @Override
            public char charAt(int index) {
                return s.charAt(index % s.length());
            }

            @Override
            public int length() {
                return s.length() * times;
            }

            @Override
            @NotNull
            public CharSequence subSequence(int start, int end) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static boolean startsWith(@Nullable CharSequence cs, @Nullable CharSequence starts) {
        if (cs == null || starts == null) {
            return false;
        }
        int l = starts.length();
        return l == 0 || cs.length() >= l && equalsChars(cs, starts, l);
    }

    // Pattern has to be lower-case.
    public static boolean startsWithLowerCase(@Nullable CharSequence cs, @Nullable CharSequence startsLC) {
        if (cs == null || startsLC == null) {
            return false;
        }

        int l = startsLC.length();
        if (l == 0) {
            return true;
        }

        return cs.length() >= l && equalsCharsLowerCase(startsLC, cs, l);
    }

    public static char toLowerCaseAscii(char character) {
        return character > 64 && character < 91 ? (char) (character + 32) : character;
    }

    public static String toString(CharSequence s) {
        return s == null ? null : s.toString();
    }

    private static int[] base64CreateInvertedAlphabet(char[] alphabet) {
        int[] inverted = new int[128]; // ASCII only
        Arrays.fill(inverted, (byte) -1);
        int length = alphabet.length;
        for (int i = 0; i < length; i++) {
            char letter = alphabet[i];
            assert letter < 128;
            inverted[letter] = (byte) i;
        }
        return inverted;
    }

    private static void base64Decode(@Nullable CharSequence encoded, @NotNull Utf8Sink target, int[] invertedAlphabet) {
        if (encoded == null) {
            return;
        }

        // skip trailing '=' they are just for padding and have no meaning
        int length = encoded.length();
        for (; length > 0; length--) {
            if (encoded.charAt(length - 1) != '=') {
                break;
            }
        }

        int remainder = length % 4;
        int sourcePos = 0;

        // first decode all 4 byte chunks. this is *the* hot loop, be careful when changing it
        for (int end = length - remainder; sourcePos < end; sourcePos += 4) {
            int b0 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos)) << 18;
            int b1 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 1)) << 12;
            int b2 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 2)) << 6;
            int b4 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 3));

            int wrk = b0 | b1 | b2 | b4;
            // we use absolute positions to write to the byte buffer in the hot loop
            // benchmarking shows that it is faster than using relative positions
            target.putAny((byte) (wrk >>> 16));
            target.putAny((byte) ((wrk >>> 8) & 0xFF));
            target.putAny((byte) (wrk & 0xFF));
        }
        // now decode remainder
        int wrk;
        switch (remainder) {
            case 0:
                // nothing to do, yay!
                break;
            case 1:
                // invalid encoding, we can't have 1 byte remainder as
                // even 1 byte encodes to 2 chars
                throw new IllegalArgumentException("invalid base64 encoding: " + encoded);
            case 2:
                wrk = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos)) << 18;
                wrk |= base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 1)) << 12;
                target.putAny((byte) (wrk >>> 16));
                break;
            case 3:
                wrk = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos)) << 18;
                wrk |= base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 1)) << 12;
                wrk |= base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 2)) << 6;
                target.putAny((byte) (wrk >>> 16));
                target.putAny((byte) ((wrk >>> 8) & 0xFF));
        }
    }

    private static void base64Decode(CharSequence encoded, ByteBuffer target, int[] invertedAlphabet) {
        if (encoded == null) {
            return;
        }
        assert target != null;

        // skip trailing '=' they are just for padding and have no meaning
        int length = encoded.length();
        for (; length > 0; length--) {
            if (encoded.charAt(length - 1) != '=') {
                break;
            }
        }

        int remainder = length % 4;
        int sourcePos = 0;
        int targetPos = target.position();

        // first decode all 4 byte chunks. this is *the* hot loop, be careful when changing it
        for (int end = length - remainder; sourcePos < end; sourcePos += 4, targetPos += 3) {
            int b0 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos)) << 18;
            int b1 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 1)) << 12;
            int b2 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 2)) << 6;
            int b4 = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 3));

            int wrk = b0 | b1 | b2 | b4;
            // we use absolute positions to write to the byte buffer in the hot loop
            // benchmarking shows that it is faster than using relative positions
            target.put(targetPos, (byte) (wrk >>> 16));
            target.put(targetPos + 1, (byte) ((wrk >>> 8) & 0xFF));
            target.put(targetPos + 2, (byte) (wrk & 0xFF));
        }
        target.position(targetPos);
        // now decode remainder
        int wrk;
        switch (remainder) {
            case 0:
                // nothing to do, yay!
                break;
            case 1:
                // invalid encoding, we can't have 1 byte remainder as
                // even 1 byte encodes to 2 chars
                throw new IllegalArgumentException("invalid base64 encoding: " + encoded);
            case 2:
                wrk = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos)) << 18;
                wrk |= base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 1)) << 12;
                target.put((byte) (wrk >>> 16));
                break;
            case 3:
                wrk = base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos)) << 18;
                wrk |= base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 1)) << 12;
                wrk |= base64InvertedLookup(invertedAlphabet, encoded.charAt(sourcePos + 2)) << 6;
                target.put((byte) (wrk >>> 16));
                target.put((byte) ((wrk >>> 8) & 0xFF));
        }
    }

    private static int base64Encode(
            @Nullable BinarySequence sequence,
            int maxLength,
            @NotNull CharSink<?> buffer,
            char @NotNull [] alphabet) {
        if (sequence == null) {
            return 0;
        }
        final long len = Math.min(maxLength, sequence.length());
        int pad = 0;
        for (int i = 0; i < len; i += 3) {
            int b = ((sequence.byteAt(i) & 0xFF) << 16) & 0xFFFFFF;
            if (i + 1 < len) {
                b |= (sequence.byteAt(i + 1) & 0xFF) << 8;
            } else {
                pad++;
            }
            if (i + 2 < len) {
                b |= (sequence.byteAt(i + 2) & 0xFF);
            } else {
                pad++;
            }

            for (int j = 0; j < 4 - pad; j++) {
                int c = (b & 0xFC0000) >> 18;
                buffer.putAscii(alphabet[c]);
                b <<= 6;
            }
        }
        return pad;
    }

    private static int base64InvertedLookup(int[] invertedAlphabet, char ch) {
        if (ch > 127) {
            throw new IllegalArgumentException("non-ascii character while decoding base64: " + ch);
        }
        int index = invertedAlphabet[ch];
        if (index == -1) {
            throw new IllegalArgumentException("invalid base64 character: " + ch);
        }
        return index;
    }

    private static boolean equalsChars(@NotNull CharSequence l, @NotNull CharSequence r, int len) {
        for (int i = 0; i < len; i++) {
            if (l.charAt(i) != r.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsCharsIgnoreCase(@NotNull CharSequence l, @NotNull CharSequence r, int len) {
        for (int i = 0; i < len; i++) {
            if (Character.toLowerCase(l.charAt(i)) != Character.toLowerCase(r.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // Left side has to be lower-case.
    private static boolean equalsCharsLowerCase(@NotNull CharSequence lLC, @NotNull CharSequence r, int len) {
        for (int i = 0; i < len; i++) {
            if (lLC.charAt(i) != Character.toLowerCase(r.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
