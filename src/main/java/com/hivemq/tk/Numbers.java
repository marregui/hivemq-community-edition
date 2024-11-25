package com.hivemq.tk;

import java.util.Arrays;

public final class Numbers {
    public static final int INT_NULL = Integer.MIN_VALUE;
    public static final long LONG_NULL = Long.MIN_VALUE;
    public static final int MAX_FLOAT_SCALE = 10;
    public static final int SIGNIFICAND_WIDTH = 53;
    public static final long SIGN_BIT_MASK = 0x8000000000000000L;
    public static final char[] hexDigits =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public final static int[] hexNumbers;
    private static final long EXP_BIT_MASK = 0x7FF0000000000000L;
    private static final int EXP_SHIFT = SIGNIFICAND_WIDTH - 1;
    private static final long SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;
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

        final LongHexAppender a12 = Numbers::appendLongHex12;
        longHexAppender[9] = a12;
        longHexAppender[10] = a12;
        longHexAppender[11] = a12;
        longHexAppender[12] = a12;

        final LongHexAppender a16 = Numbers::appendLongHex16;
        longHexAppender[13] = a16;
        longHexAppender[14] = a16;
        longHexAppender[15] = a16;
        longHexAppender[16] = a16;

        final LongHexAppender a20 = Numbers::appendLongHex20;
        longHexAppender[17] = a20;
        longHexAppender[18] = a20;
        longHexAppender[19] = a20;
        longHexAppender[20] = a20;

        final LongHexAppender a24 = Numbers::appendLongHex24;
        longHexAppender[21] = a24;
        longHexAppender[22] = a24;
        longHexAppender[23] = a24;
        longHexAppender[24] = a24;

        final LongHexAppender a28 = Numbers::appendLongHex28;
        longHexAppender[25] = a28;
        longHexAppender[26] = a28;
        longHexAppender[27] = a28;
        longHexAppender[28] = a28;

        final LongHexAppender a32 = Numbers::appendLongHex32;
        longHexAppender[29] = a32;
        longHexAppender[30] = a32;
        longHexAppender[31] = a32;
        longHexAppender[32] = a32;

        final LongHexAppender a36 = Numbers::appendLongHex36;
        longHexAppender[33] = a36;
        longHexAppender[34] = a36;
        longHexAppender[35] = a36;
        longHexAppender[36] = a36;

        final LongHexAppender a40 = Numbers::appendLongHex40;
        longHexAppender[37] = a40;
        longHexAppender[38] = a40;
        longHexAppender[39] = a40;
        longHexAppender[40] = a40;

        final LongHexAppender a44 = Numbers::appendLongHex44;
        longHexAppender[41] = a44;
        longHexAppender[42] = a44;
        longHexAppender[43] = a44;
        longHexAppender[44] = a44;

        final LongHexAppender a48 = Numbers::appendLongHex48;
        longHexAppender[45] = a48;
        longHexAppender[46] = a48;
        longHexAppender[47] = a48;
        longHexAppender[48] = a48;

        final LongHexAppender a52 = Numbers::appendLongHex52;
        longHexAppender[49] = a52;
        longHexAppender[50] = a52;
        longHexAppender[51] = a52;
        longHexAppender[52] = a52;

        final LongHexAppender a56 = Numbers::appendLongHex56;
        longHexAppender[53] = a56;
        longHexAppender[54] = a56;
        longHexAppender[55] = a56;
        longHexAppender[56] = a56;

        final LongHexAppender a60 = Numbers::appendLongHex60;
        longHexAppender[57] = a60;
        longHexAppender[58] = a60;
        longHexAppender[59] = a60;
        longHexAppender[60] = a60;

        final LongHexAppender a64 = Numbers::appendLongHex64;
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

        final LongHexAppender a12 = Numbers::appendLongHex12Pad64;
        longHexAppenderPad64[9] = a12;
        longHexAppenderPad64[10] = a12;
        longHexAppenderPad64[11] = a12;
        longHexAppenderPad64[12] = a12;

        final LongHexAppender a16 = Numbers::appendLongHex16Pad64;
        longHexAppenderPad64[13] = a16;
        longHexAppenderPad64[14] = a16;
        longHexAppenderPad64[15] = a16;
        longHexAppenderPad64[16] = a16;

        final LongHexAppender a20 = Numbers::appendLongHex20Pad64;
        longHexAppenderPad64[17] = a20;
        longHexAppenderPad64[18] = a20;
        longHexAppenderPad64[19] = a20;
        longHexAppenderPad64[20] = a20;

        final LongHexAppender a24 = Numbers::appendLongHex24Pad64;
        longHexAppenderPad64[21] = a24;
        longHexAppenderPad64[22] = a24;
        longHexAppenderPad64[23] = a24;
        longHexAppenderPad64[24] = a24;

        final LongHexAppender a28 = Numbers::appendLongHex28Pad64;
        longHexAppenderPad64[25] = a28;
        longHexAppenderPad64[26] = a28;
        longHexAppenderPad64[27] = a28;
        longHexAppenderPad64[28] = a28;

        final LongHexAppender a32 = Numbers::appendLongHex32Pad64;
        longHexAppenderPad64[29] = a32;
        longHexAppenderPad64[30] = a32;
        longHexAppenderPad64[31] = a32;
        longHexAppenderPad64[32] = a32;

        final LongHexAppender a36 = Numbers::appendLongHex36Pad64;
        longHexAppenderPad64[33] = a36;
        longHexAppenderPad64[34] = a36;
        longHexAppenderPad64[35] = a36;
        longHexAppenderPad64[36] = a36;

        final LongHexAppender a40 = Numbers::appendLongHex40Pad64;
        longHexAppenderPad64[37] = a40;
        longHexAppenderPad64[38] = a40;
        longHexAppenderPad64[39] = a40;
        longHexAppenderPad64[40] = a40;

        final LongHexAppender a44 = Numbers::appendLongHex44Pad64;
        longHexAppenderPad64[41] = a44;
        longHexAppenderPad64[42] = a44;
        longHexAppenderPad64[43] = a44;
        longHexAppenderPad64[44] = a44;

        final LongHexAppender a48 = Numbers::appendLongHex48Pad64;
        longHexAppenderPad64[45] = a48;
        longHexAppenderPad64[46] = a48;
        longHexAppenderPad64[47] = a48;
        longHexAppenderPad64[48] = a48;

        final LongHexAppender a52 = Numbers::appendLongHex52Pad64;
        longHexAppenderPad64[49] = a52;
        longHexAppenderPad64[50] = a52;
        longHexAppenderPad64[51] = a52;
        longHexAppenderPad64[52] = a52;

        final LongHexAppender a56 = Numbers::appendLongHex56Pad64;
        longHexAppenderPad64[53] = a56;
        longHexAppenderPad64[54] = a56;
        longHexAppenderPad64[55] = a56;
        longHexAppenderPad64[56] = a56;

        final LongHexAppender a60 = Numbers::appendLongHex60;
        longHexAppenderPad64[57] = a60;
        longHexAppenderPad64[58] = a60;
        longHexAppenderPad64[59] = a60;
        longHexAppenderPad64[60] = a60;

        final LongHexAppender a64 = Numbers::appendLongHex64;
        longHexAppenderPad64[61] = a64;
        longHexAppenderPad64[62] = a64;
        longHexAppenderPad64[63] = a64;
        longHexAppenderPad64[64] = a64;
    }

    private Numbers() {
    }

    public static void append(final CharSink<?> sink, final float value, final int scale) {
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
        final int scaled = (int) (f * factor + 0.5);
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

    public static void append(final CharSink<?> sink, final int value) {
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

    public static void append(final CharSink<?> sink, final long value) {
        append(sink, value, true);
    }

    public static void append(final CharSink<?> sink, final long value, final boolean checkNaN) {
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

    public static void append(final CharSink<?> sink, final float value) {
        append(sink, value, MAX_FLOAT_SCALE);
    }

    public static void append(final CharSink<?> sink, final double value) {
        final long doubleBits = Double.doubleToRawLongBits(value);
        final boolean negative = (doubleBits & SIGN_BIT_MASK) != 0L;
        long significantBitCount = doubleBits & SIGNIF_BIT_MASK;
        int binExp = (int) ((doubleBits & EXP_BIT_MASK) >> EXP_SHIFT);
        if (binExp == 2047) {
            if (significantBitCount == 0L) {
                if (negative) {
                    sink.putAscii("-Infinity");
                } else {
                    sink.putAscii("Infinity");
                }
            } else {
                sink.putAscii("NaN");
            }
        } else {
            if (binExp == 0) {
                if (significantBitCount == 0L) {
                    if (negative) {
                        sink.putAscii("-0.0");
                    } else {
                        sink.putAscii("0.0");
                    }
                    return;
                }

                final int leadingZeros = Long.numberOfLeadingZeros(significantBitCount);
                final int shift = leadingZeros - (63 - EXP_SHIFT);
                binExp = 1 - shift;
            }
            appendDouble0();
        }
    }

    public static void appendHex(final CharSink<?> sink, final long value, final boolean pad) {
        final int bit = value == 0 ? 0 : 64 - Long.numberOfLeadingZeros(value);
        final LongHexAppender[] array = pad ? longHexAppenderPad64 : longHexAppender;
        array[bit].append(sink, value);
    }

    public static void appendHexPadded(final CharSink<?> sink, final int value) {
        int i = value;
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                sink.putAscii("NaN");
                return;
            }
            sink.putAscii('-');
            i = -i;
        }
        int c;
        if (i < 0x10) {
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii(hexDigits[i]);
        } else if (i < 0x100) {  // two
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii(hexDigits[i / 0x10]);
            sink.putAscii(hexDigits[i % 0x10]);
        } else if (i < 0x1000) { // three
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii(hexDigits[i / 0x100]);
            sink.putAscii(hexDigits[(c = i % 0x100) / 0x10]);
            sink.putAscii(hexDigits[c % 0x10]);
        } else if (i < 0x10000) { // four
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii(hexDigits[i / 0x1000]);
            sink.putAscii(hexDigits[(c = i % 0x1000) / 0x100]);
            sink.putAscii(hexDigits[(c = c % 0x100) / 0x10]);
            sink.putAscii(hexDigits[c % 0x10]);
        } else if (i < 0x100000) { // five
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii(hexDigits[i / 0x10000]);
            sink.putAscii(hexDigits[(c = i % 0x10000) / 0x1000]);
            sink.putAscii(hexDigits[(c = c % 0x1000) / 0x100]);
            sink.putAscii(hexDigits[(c = c % 0x100) / 0x10]);
            sink.putAscii(hexDigits[c % 0x10]);
        } else if (i < 0x1000000) { // six
            sink.putAscii('0');
            sink.putAscii('0');
            sink.putAscii(hexDigits[i / 0x100000]);
            sink.putAscii(hexDigits[(c = i % 0x100000) / 0x10000]);
            sink.putAscii(hexDigits[(c = c % 0x10000) / 0x1000]);
            sink.putAscii(hexDigits[(c = c % 0x1000) / 0x100]);
            sink.putAscii(hexDigits[(c = c % 0x100) / 0x10]);
            sink.putAscii(hexDigits[c % 0x10]);
        } else if (i < 0x10000000) { // seven
            sink.putAscii('0');
            sink.putAscii(hexDigits[i / 0x1000000]);
            sink.putAscii(hexDigits[(c = i % 0x1000000) / 0x100000]);
            sink.putAscii(hexDigits[(c = c % 0x100000) / 0x10000]);
            sink.putAscii(hexDigits[(c = c % 0x10000) / 0x1000]);
            sink.putAscii(hexDigits[(c = c % 0x1000) / 0x100]);
            sink.putAscii(hexDigits[(c = c % 0x100) / 0x10]);
            sink.putAscii(hexDigits[c % 0x10]);
        } else { // eight
            sink.putAscii(hexDigits[i / 0x10000000]);
            sink.putAscii(hexDigits[(c = i % 0x10000000) / 0x1000000]);
            sink.putAscii(hexDigits[(c = c % 0x1000000) / 0x100000]);
            sink.putAscii(hexDigits[(c = c % 0x100000) / 0x10000]);
            sink.putAscii(hexDigits[(c = c % 0x10000) / 0x1000]);
            sink.putAscii(hexDigits[(c = c % 0x1000) / 0x100]);
            sink.putAscii(hexDigits[(c = c % 0x100) / 0x10]);
            sink.putAscii(hexDigits[c % 0x10]);
        }
    }

    public static int ceilPow2(final int value) {
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

    public static int decodeHighInt(final long val) {
        return (int) (val >> 32);
    }

    public static int decodeLowInt(final long val) {
        return (int) (val & 0xffffffffL);
    }

    public static long encodeLowHighInts(final int low, final int high) {
        return ((Integer.toUnsignedLong(high)) << 32L) | Integer.toUnsignedLong(low);
    }

    public static boolean isPow2(final int value) {
        return (value & (value - 1)) == 0;
    }

    public static int msb(final int value) {
        return 31 - Integer.numberOfLeadingZeros(value);
    }


    public static boolean notDigit(final char c) {
        return c < '0' || c > '9';
    }

    public static int parseInt(Utf8Sequence sequence) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseInt0(sequence.asAsciiCharSequence(), 0, sequence.size());
    }

    public static int parseInt(final CharSequence sequence) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }

        return parseInt0(sequence, 0, sequence.length());
    }

    public static int parseInt(final CharSequence sequence, final int p, final int lim) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseInt0(sequence, p, lim);
    }

    public static long parseInt000Greedy(final CharSequence sequence, final int p, final int lim)
            throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        final boolean negative = sequence.charAt(p) == '-';
        int i = p;
        if (negative) {
            i++;
        }

        if (i >= lim || notDigit(sequence.charAt(i))) {
            throw NumericException.INSTANCE;
        }

        int val = 0;
        for (; i < lim; i++) {
            final char c = sequence.charAt(i);

            if (notDigit(c)) {
                break;
            }

            // val * 10 + (c - '0')
            final int r = (val << 3) + (val << 1) - (c - '0');
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

    public static long parseIntSafely(final CharSequence sequence, final int p, final int lim) throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        final boolean negative = sequence.charAt(p) == '-';
        int i = p;
        if (negative) {
            i++;
        }

        if (i >= lim || notDigit(sequence.charAt(i))) {
            throw NumericException.INSTANCE;
        }

        int val = 0;
        for (; i < lim; i++) {
            final char c = sequence.charAt(i);

            if (notDigit(c)) {
                break;
            }

            // val * 10 + (c - '0')
            final int r = (val << 3) + (val << 1) - (c - '0');
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

    public static long parseLong(CharSequence sequence) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseLong0(sequence, 0, sequence.length());
    }

    public static long parseLong(final CharSequence sequence, final int p, final int lim) throws NumericException {
        if (sequence == null) {
            throw NumericException.INSTANCE;
        }
        return parseLong0(sequence, p, lim);
    }

    public static long parseLong000000Greedy(final CharSequence sequence, final int p, final int lim)
            throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        final boolean negative = sequence.charAt(p) == '-';
        int i = p;
        if (negative) {
            i++;
        }

        if (i >= lim || notDigit(sequence.charAt(i))) {
            throw NumericException.INSTANCE;
        }

        int val = 0;
        for (; i < lim; i++) {
            final char c = sequence.charAt(i);

            if (notDigit(c)) {
                break;
            }

            // val * 10 + (c - '0')
            final int r = (val << 3) + (val << 1) - (c - '0');
            if (r > val) {
                throw NumericException.INSTANCE;
            }
            val = r;
        }

        final int len = i - p;

        if (len > 6 || val == Integer.MIN_VALUE && !negative) {
            throw NumericException.INSTANCE;
        }

        while (i - p < 6) {
            val *= 10;
            i++;
        }

        return encodeLowHighInts(negative ? val : -val, len);
    }


    private static void appendDouble0(
    ) {

    }


    private static void appendInt10(final CharSink<?> sink, final int i) {
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

    private static void appendInt2(final CharSink<?> sink, final int i) {
        sink.putAscii((char) ('0' + i / 10));
        sink.putAscii((char) ('0' + i % 10));
    }

    private static void appendInt3(final CharSink<?> sink, final int i) {
        final int c;
        sink.putAscii((char) ('0' + i / 100));
        sink.putAscii((char) ('0' + (c = i % 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt4(final CharSink<?> sink, final int i) {
        int c;
        sink.putAscii((char) ('0' + i / 1000));
        sink.putAscii((char) ('0' + (c = i % 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt5(final CharSink<?> sink, final int i) {
        int c;
        sink.putAscii((char) ('0' + i / 10000));
        sink.putAscii((char) ('0' + (c = i % 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt6(final CharSink<?> sink, final int i) {
        int c;
        sink.putAscii((char) ('0' + i / 100000));
        sink.putAscii((char) ('0' + (c = i % 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt7(final CharSink<?> sink, final int i) {
        int c;
        sink.putAscii((char) ('0' + i / 1000000));
        sink.putAscii((char) ('0' + (c = i % 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendInt8(final CharSink<?> sink, final int i) {
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

    private static void appendInt9(final CharSink<?> sink, final int i) {
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

    private static void appendLong10(final CharSink<?> sink, final long i) {
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

    private static void appendLong11(final CharSink<?> sink, final long i) {
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

    private static void appendLong12(final CharSink<?> sink, final long i) {
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

    private static void appendLong13(final CharSink<?> sink, final long i) {
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

    private static void appendLong14(final CharSink<?> sink, final long i) {
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

    private static void appendLong15(final CharSink<?> sink, final long i) {
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

    private static void appendLong16(final CharSink<?> sink, final long i) {
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

    private static void appendLong17(final CharSink<?> sink, final long i) {
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

    private static void appendLong18(final CharSink<?> sink, final long i) {
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

    private static void appendLong19(final CharSink<?> sink, final long i) {
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

    private static void appendLong2(final CharSink<?> sink, final long i) {
        sink.putAscii((char) ('0' + i / 10));
        sink.putAscii((char) ('0' + i % 10));
    }

    private static void appendLong3(final CharSink<?> sink, final long i) {
        final long c;
        sink.putAscii((char) ('0' + i / 100));
        sink.putAscii((char) ('0' + (c = i % 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong4(final CharSink<?> sink, final long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000));
        sink.putAscii((char) ('0' + (c = i % 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong5(final CharSink<?> sink, final long i) {
        long c;
        sink.putAscii((char) ('0' + i / 10000));
        sink.putAscii((char) ('0' + (c = i % 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong6(final CharSink<?> sink, final long i) {
        long c;
        sink.putAscii((char) ('0' + i / 100000));
        sink.putAscii((char) ('0' + (c = i % 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong7(final CharSink<?> sink, final long i) {
        long c;
        sink.putAscii((char) ('0' + i / 1000000));
        sink.putAscii((char) ('0' + (c = i % 1000000) / 100000));
        sink.putAscii((char) ('0' + (c %= 100000) / 10000));
        sink.putAscii((char) ('0' + (c %= 10000) / 1000));
        sink.putAscii((char) ('0' + (c %= 1000) / 100));
        sink.putAscii((char) ('0' + (c %= 100) / 10));
        sink.putAscii((char) ('0' + (c % 10)));
    }

    private static void appendLong8(final CharSink<?> sink, final long i) {
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

    private static void appendLong9(final CharSink<?> sink, final long i) {
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

    private static void appendLongHex12(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 8) & 0xf)]);
        appendLongHex8(sink, value);
    }

    private static void appendLongHex12Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("000000000000");
        appendLongHex12(sink, value);
    }

    private static void appendLongHex16(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 12) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 8) & 0xf)]);
        appendLongHex8(sink, value);
    }

    private static void appendLongHex16Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("000000000000");
        appendLongHex16(sink, value);
    }

    private static void appendLongHex20(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 16) & 0xf)]);
        appendLongHex16(sink, value);
    }

    private static void appendLongHex20Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("0000000000");
        appendLongHex20(sink, value);
    }

    private static void appendLongHex24(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 20) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 16) & 0xf)]);
        appendLongHex16(sink, value);
    }

    private static void appendLongHex24Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("0000000000");
        appendLongHex24(sink, value);
    }

    private static void appendLongHex28(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 24) & 0xf)]);
        appendLongHex24(sink, value);
    }

    private static void appendLongHex28Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("00000000");
        appendLongHex28(sink, value);
    }

    private static void appendLongHex32(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 28) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 24) & 0xf)]);
        appendLongHex24(sink, value);
    }

    private static void appendLongHex32Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("00000000");
        appendLongHex32(sink, value);
    }

    private static void appendLongHex36(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 32) & 0xf)]);
        appendLongHex32(sink, value);
    }

    private static void appendLongHex36Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("000000");
        appendLongHex36(sink, value);
    }

    private static void appendLongHex4(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value) & 0xf)]);
    }

    private static void appendLongHex40(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 36) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 32) & 0xf)]);
        appendLongHex32(sink, value);
    }

    private static void appendLongHex40Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("000000");
        appendLongHex40(sink, value);
    }

    private static void appendLongHex44(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 40) & 0xf)]);
        appendLongHex40(sink, value);
    }

    private static void appendLongHex44Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("0000");
        appendLongHex44(sink, value);
    }

    private static void appendLongHex48(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 44) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 40) & 0xf)]);
        appendLongHex40(sink, value);
    }

    private static void appendLongHex48Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("0000");
        appendLongHex48(sink, value);
    }

    private static void appendLongHex4Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("00000000000000");
        appendLongHex4(sink, value);
    }

    private static void appendLongHex52(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 48) & 0xf)]);
        appendLongHex48(sink, value);
    }

    private static void appendLongHex52Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("00");
        appendLongHex52(sink, value);
    }

    private static void appendLongHex56(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 52) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 48) & 0xf)]);
        appendLongHex48(sink, value);
    }

    private static void appendLongHex56Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("00");
        appendLongHex56(sink, value);
    }

    private static void appendLongHex60(final CharSink<?> sink, final long value) {
        appendLongHexPad(sink, hexDigits[(int) ((value >> 56) & 0xf)]);
        appendLongHex56(sink, value);
    }

    private static void appendLongHex64(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 60) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value >> 56) & 0xf)]);
        appendLongHex56(sink, value);
    }

    private static void appendLongHex8(final CharSink<?> sink, final long value) {
        sink.putAscii(hexDigits[(int) ((value >> 4) & 0xf)]);
        sink.putAscii(hexDigits[(int) ((value) & 0xf)]);
    }

    private static void appendLongHex8Pad64(final CharSink<?> sink, final long value) {
        sink.putAscii("00000000000000");
        appendLongHex8(sink, value);
    }

    private static void appendLongHexPad(final CharSink<?> sink, final char hexDigit) {
        sink.putAscii('0');
        sink.putAscii(hexDigit);
    }


    private static int parseInt0(final CharSequence sequence, final int p, final int lim) throws NumericException {
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
            final char c = sequence.charAt(i);
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
                final int r = (val << 3) + (val << 1) - (c - '0');
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

    private static long parseLong0(final CharSequence sequence, final int p, final int lim) throws NumericException {
        if (lim == p) {
            throw NumericException.INSTANCE;
        }

        final boolean negative = sequence.charAt(p) == '-';

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
            final int c = sequence.charAt(i);
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
                    final long r = (val << 3) + (val << 1) - (c - '0');
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
