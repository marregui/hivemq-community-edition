package com.hivemq.tk;

import com.hivemq.tk.str.CharSink;
import com.hivemq.tk.str.NativeChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class Numbers {
    public static final int INT_NULL = Integer.MIN_VALUE;
    public static final char[] hexDigits =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public final static int[] hexNumbers;
    private static final LongHexAppender[] longHexAppender = new LongHexAppender[Long.SIZE + 1];
    private static final LongHexAppender[] longHexAppenderPad64 = new LongHexAppender[Long.SIZE + 1];
    private static final long[] pow10;

    static {
        pow10 = new long[20];
        pow10[0] = 1;
        for (int i = 1; i < pow10.length; i++) {
            pow10[i] = pow10[i - 1] * 10;
        }

        hexNumbers = new int[128];
        Arrays.fill(hexNumbers, -1);
        hexNumbers['0'] = 0;
        hexNumbers['1'] = 1;
        hexNumbers['2'] = 2;
        hexNumbers['3'] = 3;
        hexNumbers['4'] = 4;
        hexNumbers['5'] = 5;
        hexNumbers['6'] = 6;
        hexNumbers['7'] = 7;
        hexNumbers['8'] = 8;
        hexNumbers['9'] = 9;
        hexNumbers['A'] = 10;
        hexNumbers['a'] = 10;
        hexNumbers['B'] = 11;
        hexNumbers['b'] = 11;
        hexNumbers['C'] = 12;
        hexNumbers['c'] = 12;
        hexNumbers['D'] = 13;
        hexNumbers['d'] = 13;
        hexNumbers['E'] = 14;
        hexNumbers['e'] = 14;
        hexNumbers['F'] = 15;
        hexNumbers['f'] = 15;
    }

    static {
        final LongHexAppender a4 = Numbers::appendLongHex4;
        longHexAppender[0] = a4;
        longHexAppender[1] = a4;
        longHexAppender[2] = a4;
        longHexAppender[3] = a4;
        longHexAppender[4] = a4;

        final LongHexAppender a8 = Numbers::appendLongHex8;
        longHexAppender[5] = a8;
        longHexAppender[6] = a8;
        longHexAppender[7] = a8;
        longHexAppender[8] = a8;

        LongHexAppender a12 = Numbers::appendLongHex12;
        longHexAppender[9] = a12;
        longHexAppender[10] = a12;
        longHexAppender[11] = a12;
        longHexAppender[12] = a12;

        LongHexAppender a16 = Numbers::appendLongHex16;
        longHexAppender[13] = a16;
        longHexAppender[14] = a16;
        longHexAppender[15] = a16;
        longHexAppender[16] = a16;

        LongHexAppender a20 = Numbers::appendLongHex20;
        longHexAppender[17] = a20;
        longHexAppender[18] = a20;
        longHexAppender[19] = a20;
        longHexAppender[20] = a20;

        LongHexAppender a24 = Numbers::appendLongHex24;
        longHexAppender[21] = a24;
        longHexAppender[22] = a24;
        longHexAppender[23] = a24;
        longHexAppender[24] = a24;

        LongHexAppender a28 = Numbers::appendLongHex28;
        longHexAppender[25] = a28;
        longHexAppender[26] = a28;
        longHexAppender[27] = a28;
        longHexAppender[28] = a28;

        LongHexAppender a32 = Numbers::appendLongHex32;
        longHexAppender[29] = a32;
        longHexAppender[30] = a32;
        longHexAppender[31] = a32;
        longHexAppender[32] = a32;

        LongHexAppender a36 = Numbers::appendLongHex36;
        longHexAppender[33] = a36;
        longHexAppender[34] = a36;
        longHexAppender[35] = a36;
        longHexAppender[36] = a36;

        LongHexAppender a40 = Numbers::appendLongHex40;
        longHexAppender[37] = a40;
        longHexAppender[38] = a40;
        longHexAppender[39] = a40;
        longHexAppender[40] = a40;

        LongHexAppender a44 = Numbers::appendLongHex44;
        longHexAppender[41] = a44;
        longHexAppender[42] = a44;
        longHexAppender[43] = a44;
        longHexAppender[44] = a44;

        LongHexAppender a48 = Numbers::appendLongHex48;
        longHexAppender[45] = a48;
        longHexAppender[46] = a48;
        longHexAppender[47] = a48;
        longHexAppender[48] = a48;

        LongHexAppender a52 = Numbers::appendLongHex52;
        longHexAppender[49] = a52;
        longHexAppender[50] = a52;
        longHexAppender[51] = a52;
        longHexAppender[52] = a52;

        LongHexAppender a56 = Numbers::appendLongHex56;
        longHexAppender[53] = a56;
        longHexAppender[54] = a56;
        longHexAppender[55] = a56;
        longHexAppender[56] = a56;

        LongHexAppender a60 = Numbers::appendLongHex60;
        longHexAppender[57] = a60;
        longHexAppender[58] = a60;
        longHexAppender[59] = a60;
        longHexAppender[60] = a60;

        LongHexAppender a64 = Numbers::appendLongHex64;
        longHexAppender[61] = a64;
        longHexAppender[62] = a64;
        longHexAppender[63] = a64;
        longHexAppender[64] = a64;
    }

    static {
        final LongHexAppender a4 = Numbers::appendLongHex4Pad64;
        longHexAppenderPad64[0] = a4;
        longHexAppenderPad64[1] = a4;
        longHexAppenderPad64[2] = a4;
        longHexAppenderPad64[3] = a4;
        longHexAppenderPad64[4] = a4;

        final LongHexAppender a8 = Numbers::appendLongHex8Pad64;
        longHexAppenderPad64[5] = a8;
        longHexAppenderPad64[6] = a8;
        longHexAppenderPad64[7] = a8;
        longHexAppenderPad64[8] = a8;

        LongHexAppender a12 = Numbers::appendLongHex12Pad64;
        longHexAppenderPad64[9] = a12;
        longHexAppenderPad64[10] = a12;
        longHexAppenderPad64[11] = a12;
        longHexAppenderPad64[12] = a12;

        LongHexAppender a16 = Numbers::appendLongHex16Pad64;
        longHexAppenderPad64[13] = a16;
        longHexAppenderPad64[14] = a16;
        longHexAppenderPad64[15] = a16;
        longHexAppenderPad64[16] = a16;

        LongHexAppender a20 = Numbers::appendLongHex20Pad64;
        longHexAppenderPad64[17] = a20;
        longHexAppenderPad64[18] = a20;
        longHexAppenderPad64[19] = a20;
        longHexAppenderPad64[20] = a20;

        LongHexAppender a24 = Numbers::appendLongHex24Pad64;
        longHexAppenderPad64[21] = a24;
        longHexAppenderPad64[22] = a24;
        longHexAppenderPad64[23] = a24;
        longHexAppenderPad64[24] = a24;

        LongHexAppender a28 = Numbers::appendLongHex28Pad64;
        longHexAppenderPad64[25] = a28;
        longHexAppenderPad64[26] = a28;
        longHexAppenderPad64[27] = a28;
        longHexAppenderPad64[28] = a28;

        LongHexAppender a32 = Numbers::appendLongHex32Pad64;
        longHexAppenderPad64[29] = a32;
        longHexAppenderPad64[30] = a32;
        longHexAppenderPad64[31] = a32;
        longHexAppenderPad64[32] = a32;

        LongHexAppender a36 = Numbers::appendLongHex36Pad64;
        longHexAppenderPad64[33] = a36;
        longHexAppenderPad64[34] = a36;
        longHexAppenderPad64[35] = a36;
        longHexAppenderPad64[36] = a36;

        LongHexAppender a40 = Numbers::appendLongHex40Pad64;
        longHexAppenderPad64[37] = a40;
        longHexAppenderPad64[38] = a40;
        longHexAppenderPad64[39] = a40;
        longHexAppenderPad64[40] = a40;

        LongHexAppender a44 = Numbers::appendLongHex44Pad64;
        longHexAppenderPad64[41] = a44;
        longHexAppenderPad64[42] = a44;
        longHexAppenderPad64[43] = a44;
        longHexAppenderPad64[44] = a44;

        LongHexAppender a48 = Numbers::appendLongHex48Pad64;
        longHexAppenderPad64[45] = a48;
        longHexAppenderPad64[46] = a48;
        longHexAppenderPad64[47] = a48;
        longHexAppenderPad64[48] = a48;

        LongHexAppender a52 = Numbers::appendLongHex52Pad64;
        longHexAppenderPad64[49] = a52;
        longHexAppenderPad64[50] = a52;
        longHexAppenderPad64[51] = a52;
        longHexAppenderPad64[52] = a52;

        LongHexAppender a56 = Numbers::appendLongHex56Pad64;
        longHexAppenderPad64[53] = a56;
        longHexAppenderPad64[54] = a56;
        longHexAppenderPad64[55] = a56;
        longHexAppenderPad64[56] = a56;

        LongHexAppender a60 = Numbers::appendLongHex60;
        longHexAppenderPad64[57] = a60;
        longHexAppenderPad64[58] = a60;
        longHexAppenderPad64[59] = a60;
        longHexAppenderPad64[60] = a60;

        LongHexAppender a64 = Numbers::appendLongHex64;
        longHexAppenderPad64[61] = a64;
        longHexAppenderPad64[62] = a64;
        longHexAppenderPad64[63] = a64;
        longHexAppenderPad64[64] = a64;
    }

    private Numbers() {
    }

    public static void append(final @NotNull CharSink<?> sink, final float value, int scale) {
        float f = value;
        if (f == Float.POSITIVE_INFINITY) {
            sink.putAscii("Infinity");
            return;
        }

        if (f == Float.NEGATIVE_INFINITY) {
            sink.putAscii("-Infinity");
            return;
        }

        if (Float.isNaN(f)) {
            sink.putAscii("NaN");
            return;
        }
        // it is very awkward to distinguish between 0.0 and -0.0
        // -0.0 < 0 is false
        if (f < 0 || 1 / f == Float.NEGATIVE_INFINITY) {
            sink.putAscii('-');
            f = -f;
        }
        int factor = (int) pow10[scale];
        int scaled = (int) (f * factor + 0.5);
        int targetScale = scale + 1;
        int z;
        while (targetScale < 11 && (z = factor * 10) <= scaled) {
            factor = z;
            targetScale++;
        }

        if (targetScale == 11) {
            sink.putAscii(Float.toString(f));
            return;
        }
        while (targetScale > 0) {
            if (targetScale-- == scale) {
                sink.putAscii('.');
            }
            sink.putAscii((char) ('0' + scaled / factor % 10));
            factor /= 10;
        }
    }

    public static void append(CharSink<?> sink, final int value) {
        int i = value;
        if (i < 0) {
            if (i == Numbers.INT_NULL) {
                sink.putAscii("null");
                return;
            }
            sink.putAscii('-');
            i = -i;
        }
        if (i < 10) {
            sink.putAscii((char) ('0' + i));
        } else if (i < 100) {  // two
            appendInt2(sink, i);
        } else if (i < 1000) { // three
            appendInt3(sink, i);
        } else if (i < 10000) { // four
            appendInt4(sink, i);
        } else if (i < 100000) { // five
            appendInt5(sink, i);
        } else if (i < 1000000) { // six
            appendInt6(sink, i);
        } else if (i < 10000000) { // seven
            appendInt7(sink, i);
        } else if (i < 100000000) { // eight
            appendInt8(sink, i);
        } else if (i < 1000000000) { // nine
            appendInt9(sink, i);
        } else {
            // ten
            appendInt10(sink, i);
        }
    }

    public static void append(CharSink<?> sink, final long value) {
        append(sink, value, true);
    }

    public static void append(CharSink<?> sink, final long value, final boolean checkNaN) {
        long i = value;
        if (i < 0) {
            if (i == Long.MIN_VALUE) {
                if (checkNaN) {
                    sink.putAscii("null");
                } else {
                    // we cannot negate Long.MIN_VALUE, so we have to special case it
                    sink.putAscii("-9223372036854775808");
                }
                return;
            }
            sink.putAscii('-');
            i = -i;
        }

        if (i < 10) {
            sink.putAscii((char) ('0' + i));
        } else if (i < 100) {  // two
            appendLong2(sink, i);
        } else if (i < 1000) { // three
            appendLong3(sink, i);
        } else if (i < 10000) { // four
            appendLong4(sink, i);
        } else if (i < 100000) { // five
            appendLong5(sink, i);
        } else if (i < 1000000) { // six
            appendLong6(sink, i);
        } else if (i < 10000000) { // seven
            appendLong7(sink, i);
        } else if (i < 100000000) { // eight
            appendLong8(sink, i);
        } else if (i < 1000000000) { // nine
            appendLong9(sink, i);
        } else if (i < 10000000000L) {
            appendLong10(sink, i);
        } else if (i < 100000000000L) { //  eleven
            appendLong11(sink, i);
        } else if (i < 1000000000000L) { //  twelve
            appendLong12(sink, i);
        } else if (i < 10000000000000L) { //  thirteen
            appendLong13(sink, i);
        } else if (i < 100000000000000L) { //  fourteen
            appendLong14(sink, i);
        } else if (i < 1000000000000000L) { //  fifteen
            appendLong15(sink, i);
        } else if (i < 10000000000000000L) { //  sixteen
            appendLong16(sink, i);
        } else if (i < 100000000000000000L) { //  seventeen
            appendLong17(sink, i);
        } else if (i < 1000000000000000000L) { //  eighteen
            appendLong18(sink, i);
        } else { //  nineteen
            appendLong19(sink, i);
        }
    }

    public static void appendHex(CharSink<?> sink, long value, boolean pad) {
        int bit = value == 0 ? 0 : 64 - Long.numberOfLeadingZeros(value);
        LongHexAppender[] array = pad ? longHexAppenderPad64 : longHexAppender;
        array[bit].append(sink, value);
    }

    public static int ceilPow2(int value) {
        int i = value;
        if ((i != 0) && (i & (i - 1)) > 0) {
            i |= (i >>> 1);
            i |= (i >>> 2);
            i |= (i >>> 4);
            i |= (i >>> 8);
            i |= (i >>> 16);
            i++;

            if (i < 0) {
                i >>>= 1;
            }
        }

        return i;
    }

    public static int decodeHighInt(long val) {
        return (int) (val >> 32);
    }

    public static int decodeLowInt(long val) {
        return (int) (val & 0xffffffffL);
    }

    public static long encodeLowHighInts(int low, int high) {
        return ((Integer.toUnsignedLong(high)) << 32L) | Integer.toUnsignedLong(low);
    }

    public static boolean isPow2(int value) {
        return (value & (value - 1)) == 0;
    }

    public static int msb(int value) {
        return 31 - Integer.numberOfLeadingZeros(value);
    }

    public static boolean notDigit(char c) {
        return c < '0' || c > '9';
    }

    public static int parseInt(NativeChunk sequence) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseInt0(sequence.asAsciiCharSequence(), 0, sequence.size());
    }

    public static int parseInt(CharSequence sequence) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }

        return parseInt0(sequence, 0, sequence.length());
    }

    public static long parseInt000Greedy(CharSequence sequence, final int p, int lim) throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        boolean negative = sequence.charAt(p) == '-';
        int i = p;
        if (negative) {
            i++;
        }

        if (i >= lim || notDigit(sequence.charAt(i))) {
            throw NumericException.INSTANCE;
        }

        int val = 0;
        for (; i < lim; i++) {
            char c = sequence.charAt(i);

            if (notDigit(c)) {
                break;
            }

            // val * 10 + (c - '0')
            int r = (val << 3) + (val << 1) - (c - '0');
            if (r > val) {
                throw NumericException.INSTANCE;
            }
            val = r;
        }

        final int len = i - p;

        if (len > 3 || val == Integer.MIN_VALUE && !negative) {
            throw NumericException.INSTANCE;
        }

        while (i - p < 3) {
            val *= 10;
            i++;
        }

        return encodeLowHighInts(negative ? val : -val, len);
    }

    public static long parseIntSafely(CharSequence sequence, final int p, int lim) throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        boolean negative = sequence.charAt(p) == '-';
        int i = p;
        if (negative) {
            i++;
        }

        if (i >= lim || notDigit(sequence.charAt(i))) {
            throw NumericException.INSTANCE;
        }

        int val = 0;
        for (; i < lim; i++) {
            char c = sequence.charAt(i);

            if (notDigit(c)) {
                break;
            }

            // val * 10 + (c - '0')
            int r = (val << 3) + (val << 1) - (c - '0');
            if (r > val) {
                throw NumericException.INSTANCE;
            }
            val = r;
        }

        if (val == Integer.MIN_VALUE && !negative) {
            throw NumericException.INSTANCE;
        }

        return encodeLowHighInts(negative ? val : -val, i - p);
    }

    public static int parseIntSize(CharSequence sequence) throws NumericException {
        int lim = sequence.length();

        if (lim == 0) {
            throw NumericException.INSTANCE;
        }

        boolean negative = sequence.charAt(0) == '-';
        int i = 0;
        if (negative) {
            i++;
        }

        if (i >= lim) {
            throw NumericException.INSTANCE;
        }

        int val = 0;
        int r;
        EX:
        for (; i < lim; i++) {
            int c = sequence.charAt(i);
            if (c < '0' || c > '9') {
                if (i == lim - 1) {
                    switch (c) {
                        case 'K':
                        case 'k':
                            r = val * 1024;
                            if (r > val) {
                                throw NumericException.INSTANCE;
                            }
                            val = r;
                            break EX;
                        case 'M':
                        case 'm':
                            r = val * 1024 * 1024;
                            if (r > val) {
                                throw NumericException.INSTANCE;
                            }
                            val = r;
                            break EX;
                        default:
                            break;
                    }
                }
                throw NumericException.INSTANCE;
            }
            // val * 10 + (c - '0')
            r = (val << 3) + (val << 1) - (c - '0');
            if (r > val) {
                throw NumericException.INSTANCE;
            }
            val = r;
        }

        if (val == Integer.MIN_VALUE && !negative) {
            throw NumericException.INSTANCE;
        }
        return negative ? val : -val;
    }

    public static long parseLong(CharSequence sequence) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseLong0(sequence, 0, sequence.length());
    }

    public static long parseLong(CharSequence sequence, int p, int lim) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseLong0(sequence, p, lim);
    }

    private static void appendInt10(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 1000000000));
        sink.putAscii((char) ('0' + (c = i % 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt2(CharSink<?> sink, int i) {
        sink.putAscii((char) ('0' + i / 10));
        sink.putAscii((char) ('0' + i % 10));
    }

    private static void appendInt3(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 100));
        sink.putAscii((char) ('0' + (c = i % 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt4(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 1000));
        sink.putAscii((char) ('0' + (c = i % 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt5(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 10000));
        sink.putAscii((char) ('0' + (c = i % 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt6(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 100000));
        sink.putAscii((char) ('0' + (c = i % 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt7(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 1000000));
        sink.putAscii((char) ('0' + (c = i % 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt8(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 10000000));
        sink.putAscii((char) ('0' + (c = i % 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt9(CharSink<?> sink, int i) {
        int c;
        sink.putAscii((char) ('0' + i / 100000000));
        sink.putAscii((char) ('0' + (c = i % 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong10(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000000000L));
        sink.putAscii((char) ('0' + (c = i % 1000000000L) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong11(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 10000000000L));
        sink.putAscii((char) ('0' + (c = i % 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong12(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100000000000L));
        sink.putAscii((char) ('0' + (c = i % 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong13(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000000000000L));
        sink.putAscii((char) ('0' + (c = i % 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong14(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 10000000000000L));
        sink.putAscii((char) ('0' + (c = i % 10000000000000L) / 1000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong15(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100000000000000L));
        sink.putAscii((char) ('0' + (c = i % 100000000000000L) / 10000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000L) / 1000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong16(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000000000000000L));
        sink.putAscii((char) ('0' + (c = i % 1000000000000000L) / 100000000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000000L) / 10000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000L) / 1000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong17(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 10000000000000000L));
        sink.putAscii((char) ('0' + (c = i % 10000000000000000L) / 1000000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000000L) / 100000000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000000L) / 10000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000L) / 1000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong18(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100000000000000000L));
        sink.putAscii((char) ('0' + (c = i % 100000000000000000L) / 10000000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000000L) / 1000000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000000L) / 100000000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000000L) / 10000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000L) / 1000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong19(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000000000000000000L));
        sink.putAscii((char) ('0' + (c = i % 1000000000000000000L) / 100000000000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000000000L) / 10000000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000000L) / 1000000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000000L) / 100000000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000000L) / 10000000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000000L) / 1000000000000L));
        sink.putAscii((char) ('0' + (c %= 1000000000000L) / 100000000000L));
        sink.putAscii((char) ('0' + (c %= 100000000000L) / 10000000000L));
        sink.putAscii((char) ('0' + (c %= 10000000000L) / 1000000000));
        sink.putAscii((char) ('0' + (c %= 1000000000) / 100000000));
        sink.putAscii((char) ('0' + (c %= 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong2(CharSink<?> sink, long i) {
        sink.putAscii((char) ('0' + i / 10));
        sink.putAscii((char) ('0' + i % 10));
    }

    private static void appendLong3(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100));
        sink.putAscii((char) ('0' + (c = i % 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong4(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000));
        sink.putAscii((char) ('0' + (c = i % 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong5(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 10000));
        sink.putAscii((char) ('0' + (c = i % 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong6(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100000));
        sink.putAscii((char) ('0' + (c = i % 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong7(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000000));
        sink.putAscii((char) ('0' + (c = i % 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong8(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 10000000));
        sink.putAscii((char) ('0' + (c = i % 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong9(CharSink<?> sink, long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100000000));
        sink.putAscii((char) ('0' + (c = i % 100000000) / 10000000));
        sink.putAscii((char) ('0' + (c %= 10000000) / 1000000));
        sink.putAscii((char) ('0' + (c %= 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLongHex12(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 8) & 0xf)]);
        appendLongHex8(sink, value);
    }

    private static void appendLongHex12Pad64(CharSink<?> sink, long value) {
        sink.putAscii("000000000000");
        appendLongHex12(sink, value);
    }

    private static void appendLongHex16(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 12) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 8) & 0xf)]);
        appendLongHex8(sink, value);
    }

    private static void appendLongHex16Pad64(CharSink<?> sink, long value) {
        sink.putAscii("000000000000");
        appendLongHex16(sink, value);
    }

    private static void appendLongHex20(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 16) & 0xf)]);
        appendLongHex16(sink, value);
    }

    private static void appendLongHex20Pad64(CharSink<?> sink, long value) {
        sink.putAscii("0000000000");
        appendLongHex20(sink, value);
    }

    private static void appendLongHex24(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 20) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 16) & 0xf)]);
        appendLongHex16(sink, value);
    }

    private static void appendLongHex24Pad64(CharSink<?> sink, long value) {
        sink.putAscii("0000000000");
        appendLongHex24(sink, value);
    }

    private static void appendLongHex28(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 24) & 0xf)]);
        appendLongHex24(sink, value);
    }

    private static void appendLongHex28Pad64(CharSink<?> sink, long value) {
        sink.putAscii("00000000");
        appendLongHex28(sink, value);
    }

    private static void appendLongHex32(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 28) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 24) & 0xf)]);
        appendLongHex24(sink, value);
    }

    private static void appendLongHex32Pad64(CharSink<?> sink, long value) {
        sink.putAscii("00000000");
        appendLongHex32(sink, value);
    }

    private static void appendLongHex36(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 32) & 0xf)]);
        appendLongHex32(sink, value);
    }

    private static void appendLongHex36Pad64(CharSink<?> sink, long value) {
        sink.putAscii("000000");
        appendLongHex36(sink, value);
    }

    private static void appendLongHex4(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value) & 0xf)]);
    }

    private static void appendLongHex40(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 36) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 32) & 0xf)]);
        appendLongHex32(sink, value);
    }

    private static void appendLongHex40Pad64(CharSink<?> sink, long value) {
        sink.putAscii("000000");
        appendLongHex40(sink, value);
    }

    private static void appendLongHex44(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 40) & 0xf)]);
        appendLongHex40(sink, value);
    }

    private static void appendLongHex44Pad64(CharSink<?> sink, long value) {
        sink.putAscii("0000");
        appendLongHex44(sink, value);
    }

    private static void appendLongHex48(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 44) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 40) & 0xf)]);
        appendLongHex40(sink, value);
    }

    private static void appendLongHex48Pad64(CharSink<?> sink, long value) {
        sink.putAscii("0000");
        appendLongHex48(sink, value);
    }

    private static void appendLongHex4Pad64(CharSink<?> sink, long value) {
        sink.putAscii("00000000000000");
        appendLongHex4(sink, value);
    }

    private static void appendLongHex52(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 48) & 0xf)]);
        appendLongHex48(sink, value);
    }

    private static void appendLongHex52Pad64(CharSink<?> sink, long value) {
        sink.putAscii("00");
        appendLongHex52(sink, value);
    }

    private static void appendLongHex56(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 52) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 48) & 0xf)]);
        appendLongHex48(sink, value);
    }

    private static void appendLongHex56Pad64(CharSink<?> sink, long value) {
        sink.putAscii("00");
        appendLongHex56(sink, value);
    }

    private static void appendLongHex60(CharSink<?> sink, long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 56) & 0xf)]);
        appendLongHex56(sink, value);
    }

    private static void appendLongHex64(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 60) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 56) & 0xf)]);
        appendLongHex56(sink, value);
    }

    private static void appendLongHex8(CharSink<?> sink, long value) {
        sink.putAscii(hexDigits[(int) ((value >> 4) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value) & 0xf)]);
    }

    private static void appendLongHex8Pad64(CharSink<?> sink, long value) {
        sink.putAscii("00000000000000");
        appendLongHex8(sink, value);
    }

    private static void appendLongHexPad(CharSink<?> sink, char hexDigit) {
        sink.putAscii('0');
        sink.putAscii(hexDigit);
    }

    private static int parseInt0(CharSequence sequence, final int p, int lim) throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        final char sign = sequence.charAt(p);
        final boolean negative = sign == '-';
        int i = p;
        if (negative || sign == '+') {
            i++;
        }

        if (i >= lim) {
            throw NumericException.INSTANCE;
        }

        int digitCounter = 0;
        int val = 0;
        for (; i < lim; i++) {
            char c = sequence.charAt(i);
            if (c == '_') {
                if (digitCounter == 0) {
                    throw NumericException.INSTANCE;
                }
                digitCounter = 0;
            } else if (c < '0' || c > '9') {
                throw NumericException.INSTANCE;
            } else {
                // val * 10 + (c - '0')
                if (val < (Integer.MIN_VALUE / 10)) {
                    throw NumericException.INSTANCE;
                }
                int r = (val << 3) + (val << 1) - (c - '0');
                if (r > val) {
                    throw NumericException.INSTANCE;
                }
                val = r;
                digitCounter++;
            }
        }

        if ((val == Integer.MIN_VALUE && !negative) || digitCounter == 0) {
            throw NumericException.INSTANCE;
        }
        return negative ? val : -val;
    }

    private static long parseLong0(CharSequence sequence, final int p, int lim) throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        boolean negative = sequence.charAt(p) == '-';

        int i = p;
        if (negative) {
            i++;
        }

        if (i >= lim) {
            throw NumericException.INSTANCE;
        }

        int digitCounter = 0;
        long val = 0;
        for (; i < lim; i++) {
            int c = sequence.charAt(i);
            switch (c | 32) {
                case 'l':
                    if (i == 0 || i + 1 < lim) {
                        throw NumericException.INSTANCE;
                    }
                    break;
                case 127: // '_'
                    if (digitCounter == 0) {
                        throw NumericException.INSTANCE;
                    }
                    digitCounter = 0;
                    break;
                default:
                    if (c < '0' || c > '9') {
                        throw NumericException.INSTANCE;
                    }
                    // val * 10 + (c - '0')
                    long r = (val << 3) + (val << 1) - (c - '0');
                    if (r > val) {
                        throw NumericException.INSTANCE;
                    }
                    val = r;
                    digitCounter++;
            }
        }

        if ((val == Long.MIN_VALUE && !negative) || digitCounter == 0) {
            throw NumericException.INSTANCE;
        }
        return negative ? val : -val;
    }

    @FunctionalInterface
    private interface LongHexAppender {
        void append(CharSink<?> sink, long value);
    }
}
