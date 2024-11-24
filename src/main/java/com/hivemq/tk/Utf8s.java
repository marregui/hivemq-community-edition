package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public final class Utf8s {
    public static final int VARCHAR_INLINED_PREFIX_BYTES = 6;
    public static final long VARCHAR_INLINED_PREFIX_MASK = (1L << 8 * VARCHAR_INLINED_PREFIX_BYTES) - 1L;
    private static final ThreadLocal<StringSink> tlSink = ThreadLocal.withInitial(() -> new StringSink());

    private Utf8s() {
    }

    public static int encodeUtf16Char(@NotNull Utf8Sink sink, @NotNull CharSequence cs, int hi, int i, char c) {
        if (c < 2048) {
            sink.put((byte) (192 | c >> 6));
            sink.put((byte) (128 | c & 63));
        } else if (Character.isSurrogate(c)) {
            i = encodeUtf16Surrogate(sink, c, cs, i, hi);
        } else {
            sink.put((byte) (224 | c >> 12));
            sink.put((byte) (128 | c >> 6 & 63));
            sink.put((byte) (128 | c & 63));
        }
        return i;
    }

    public static boolean equalsIgnoreCaseAscii(@NotNull CharSequence asciiSeq, final @NotNull Utf8Sequence seq) {
        int len = asciiSeq.length();
        if (len != seq.size()) {
            return false;
        }
        for (int index = 0; index < len; index++) {
            if (Chars.toLowerCaseAscii(asciiSeq.charAt(index)) != toLowerCaseAscii(seq.byteAt(index))) {
                return false;
            }
        }
        return true;
    }

    public static int getUtf8Codepoint(int b1, int b2, int b3, int b4) {
        return b1 << 18 ^ b2 << 12 ^ b3 << 6 ^ b4 ^ 3678080;
    }

    public static void strCpy(@NotNull Utf8Sequence src, int destLen, long destAddr) {
        for (int i = 0; i < destLen; i++) {
            Unsafe.UNSAFE.putByte(destAddr + i, src.byteAt(i));
        }
    }

    public static void strCpyAscii(@NotNull CharSequence asciiSrc, int srcLen, long destAddr) {
        strCpyAscii(asciiSrc, 0, srcLen, destAddr);
    }

    public static void strCpyAscii(@NotNull CharSequence asciiSrc, int srcLo, int srcLen, long destAddr) {
        for (int i = 0; i < srcLen; i++) {
            Unsafe.UNSAFE.putByte(destAddr + i, (byte) asciiSrc.charAt(srcLo + i));
        }
    }

    public static String stringFromUtf8Bytes(long lo, long hi) {
        if (hi == lo) {
            return "";
        }
        Utf16Sink b = getThreadLocalSink();
        utf8ToUtf16(lo, hi, b);
        return b.toString();
    }

    public static String stringFromUtf8Bytes(final @NotNull Utf8Sequence seq) {
        if (seq.size() == 0) {
            return "";
        }
        Utf16Sink b = getThreadLocalSink();
        utf8ToUtf16(seq, b);
        return b.toString();
    }


    public static int utf8DecodeMultiByte(long lo, long hi, byte b, Utf16Sink sink) {
        if (b >> 5 == -2 && (b & 30) != 0) {
            return utf8Decode2Bytes(lo, hi, b, sink);
        }
        if (b >> 4 == -2) {
            return utf8Decode3Bytes(lo, hi, b, sink);
        }
        return utf8Decode4Bytes(lo, hi, b, sink);
    }

    public static char utf8ToChar(byte b1, byte b2, byte b3) {
        return (char) (b1 << 12 ^ b2 << 6 ^ b3 ^ -123008);
    }

    /**
     * Decodes bytes between lo,hi addresses into sink.
     * Note: operation might fail in the middle and leave sink in inconsistent state.
     *
     * @return true if input is proper UTF-8 and false otherwise.
     */
    public static boolean utf8ToUtf16(long lo, long hi, @NotNull Utf16Sink sink) {
        long p = lo;
        while (p < hi) {
            byte b = Unsafe.UNSAFE.getByte(p);
            if (b < 0) {
                int n = utf8DecodeMultiByte(p, hi, b, sink);
                if (n == -1) {
                    // UTF8 error
                    return false;
                }
                p += n;
            } else {
                sink.put((char) b);
                ++p;
            }
        }
        return true;
    }

    /**
     * Decodes bytes from the given UTF-8 sink into char sink.
     * Note: operation might fail in the middle and leave sink in inconsistent state.
     *
     * @param seq   input sequence
     * @param seqLo character bytes start in input sequence
     * @param seqHi character bytes end in input sequence (exclusive)
     * @param sink  destination sink
     * @return true if input is proper UTF-8 and false otherwise.
     */
    public static boolean utf8ToUtf16(final @NotNull Utf8Sequence seq, int seqLo, int seqHi, @NotNull Utf16Sink sink) {
        int i = seqLo;
        while (i < seqHi) {
            byte b = seq.byteAt(i);
            if (b < 0) {
                int n = utf8DecodeMultiByte(seq, i, b, sink);
                if (n == -1) {
                    // UTF-8 error
                    return false;
                }
                i += n;
            } else {
                sink.put((char) b);
                ++i;
            }
        }
        return true;
    }

    /**
     * Decodes bytes from the given UTF-8 sink into char sink.
     * Note: operation might fail in the middle and leave sink in inconsistent state.
     *
     * @return true if input is proper UTF-8 and false otherwise.
     */
    public static boolean utf8ToUtf16(final @NotNull Utf8Sequence seq, @NotNull Utf16Sink sink) {
        return utf8ToUtf16(seq, 0, seq.size(), sink);
    }

    /**
     * Returns up to 6 initial bytes of the given UTF-8 sequence (less if it's shorter)
     * packed into a zero-padded long value, in little-endian order. This prefix is
     * stored inline in the auxiliary vector of a VARCHAR column, so asking for it is a
     * matter of optimized data access. This is not a general access method, it
     * shouldn't be called unless looking to optimize the access of the VARCHAR column.
     *
     * @param seq UTF8 sequence
     * @return up to 6 initial bytes
     */
    public static long zeroPaddedSixPrefix(final @NotNull Utf8Sequence seq) {
        final int size = seq.size();
        if (size >= Long.BYTES) {
            return seq.longAt(0) & VARCHAR_INLINED_PREFIX_MASK;
        }
        final long limit = Math.min(size, VARCHAR_INLINED_PREFIX_BYTES);
        long result = 0;
        for (int i = 0; i < limit; i++) {
            result |= (seq.byteAt(i) & 0xffL) << (8 * i);
        }
        return result;
    }


    private static int encodeUtf16Surrogate(@NotNull Utf8Sink sink, char c, @NotNull CharSequence in, int pos, int hi) {
        int dword;
        if (Character.isHighSurrogate(c)) {
            if (hi - pos < 1) {
                sink.putAscii('?');
                return pos;
            } else {
                char c2 = in.charAt(pos++);
                if (Character.isLowSurrogate(c2)) {
                    dword = Character.toCodePoint(c, c2);
                } else {
                    sink.putAscii('?');
                    return pos;
                }
            }
        } else if (Character.isLowSurrogate(c)) {
            sink.putAscii('?');
            return pos;
        } else {
            dword = c;
        }
        sink.put((byte) (240 | dword >> 18));
        sink.put((byte) (128 | dword >> 12 & 63));
        sink.put((byte) (128 | dword >> 6 & 63));
        sink.put((byte) (128 | dword & 63));
        return pos;
    }

    private static StringSink getThreadLocalSink() {
        StringSink b = tlSink.get();
        b.clear();
        return b;
    }

    private static boolean isMalformed3(int b1, int b2, int b3) {
        return b1 == -32 && (b2 & 224) == 128 || (b2 & 192) != 128 || (b3 & 192) != 128;
    }

    private static boolean isMalformed4(int b2, int b3, int b4) {
        return (b2 & 192) != 128 || (b3 & 192) != 128 || (b4 & 192) != 128;
    }

    private static boolean isNotContinuation(int b) {
        return (b & 192) != 128;
    }

    private static byte toLowerCaseAscii(byte b) {
        return b > 64 && b < 91 ? (byte) (b + 32) : b;
    }

    private static int utf8Decode2Bytes(final @NotNull Utf8Sequence seq, int index, int b1, @NotNull Utf16Sink sink) {
        if (seq.size() - index < 2) {
            return -1;
        }
        byte b2 = seq.byteAt(index + 1);
        if (isNotContinuation(b2)) {
            return -1;
        }
        sink.put((char) (b1 << 6 ^ b2 ^ 3968));
        return 2;
    }

    private static int utf8Decode2Bytes(long lo, long hi, int b1, @NotNull Utf16Sink sink) {
        if (hi - lo < 2) {
            return -1;
        }
        byte b2 = Unsafe.UNSAFE.getByte(lo + 1);
        if (isNotContinuation(b2)) {
            return -1;
        }
        sink.put((char) (b1 << 6 ^ b2 ^ 3968));
        return 2;
    }

    private static int utf8Decode3Byte0(byte b1, @NotNull Utf16Sink sink, byte b2, byte b3) {
        if (isMalformed3(b1, b2, b3)) {
            return -1;
        }
        char c = utf8ToChar(b1, b2, b3);
        if (Character.isSurrogate(c)) {
            return -1;
        }
        sink.put(c);
        return 3;
    }

    private static int utf8Decode3Bytes(long lo, long hi, byte b1, @NotNull Utf16Sink sink) {
        if (hi - lo < 3) {
            return -1;
        }
        byte b2 = Unsafe.UNSAFE.getByte(lo + 1);
        byte b3 = Unsafe.UNSAFE.getByte(lo + 2);
        return utf8Decode3Byte0(b1, sink, b2, b3);
    }

    private static int utf8Decode3Bytes(final @NotNull Utf8Sequence seq, int index, byte b1, @NotNull Utf16Sink sink) {
        if (seq.size() - index < 3) {
            return -1;
        }
        byte b2 = seq.byteAt(index + 1);
        byte b3 = seq.byteAt(index + 2);
        return utf8Decode3Byte0(b1, sink, b2, b3);
    }

    private static int utf8Decode4Bytes(long lo, long hi, int b, @NotNull Utf16Sink sink) {
        if (b >> 3 != -2 || hi - lo < 4) {
            return -1;
        }
        byte b2 = Unsafe.UNSAFE.getByte(lo + 1);
        byte b3 = Unsafe.UNSAFE.getByte(lo + 2);
        byte b4 = Unsafe.UNSAFE.getByte(lo + 3);
        return utf8Decode4Bytes0(b, sink, b2, b3, b4);
    }

    private static int utf8Decode4Bytes(final @NotNull Utf8Sequence seq, int index, int b, @NotNull Utf16Sink sink) {
        if (b >> 3 != -2 || seq.size() - index < 4) {
            return -1;
        }
        byte b2 = seq.byteAt(index + 1);
        byte b3 = seq.byteAt(index + 2);
        byte b4 = seq.byteAt(index + 3);
        return utf8Decode4Bytes0(b, sink, b2, b3, b4);
    }

    private static int utf8Decode4Bytes0(int b, @NotNull Utf16Sink sink, byte b2, byte b3, byte b4) {
        if (isMalformed4(b2, b3, b4)) {
            return -1;
        }
        final int codePoint = getUtf8Codepoint(b, b2, b3, b4);
        if (Character.isSupplementaryCodePoint(codePoint)) {
            sink.put(Character.highSurrogate(codePoint));
            sink.put(Character.lowSurrogate(codePoint));
            return 4;
        }
        return -1;
    }

    private static int utf8DecodeMultiByte(
            final @NotNull Utf8Sequence seq,
            int index,
            byte b,
            @NotNull Utf16Sink sink) {
        if (b >> 5 == -2 && (b & 30) != 0) {
            // we should allow 11000001, as it is a valid UTF8 byte?
            return utf8Decode2Bytes(seq, index, b, sink);
        }
        if (b >> 4 == -2) {
            return utf8Decode3Bytes(seq, index, b, sink);
        }
        return utf8Decode4Bytes(seq, index, b, sink);
    }
}
