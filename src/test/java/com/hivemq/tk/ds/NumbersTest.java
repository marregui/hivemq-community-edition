

package com.hivemq.tk.ds;

import com.hivemq.tk.Numbers;
import com.hivemq.tk.NumericException;
import com.hivemq.tk.Rnd;
import com.hivemq.tk.str.StringSink;
import com.hivemq.tk.TestUtils;
import com.hivemq.tk.str.Utf8String;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NumbersTest {
    private StringSink sink;
    private Rnd rnd;

    private static void assertParseLongException(String input) {
        try {
            Numbers.parseLong(input);
            Assert.fail();
        } catch (NumericException ignore) {
        }
    }

    @Before
    public void setUp() {
        sink = new StringSink();
        rnd = new Rnd();
    }

    @Test
    public void parseExplicitLong() throws Exception {
        Assert.assertEquals(10000L, Numbers.parseLong("10000L"));
    }

    @Test(expected = NumericException.class)
    public void parseExplicitLong2() throws Exception {
        Numbers.parseLong("10000LL");
    }

    @Test
    public void testCeilPow2() {
        Assert.assertEquals(16, Numbers.ceilPow2(15));
        Assert.assertEquals(16, Numbers.ceilPow2(16));
        Assert.assertEquals(32, Numbers.ceilPow2(17));
    }

    @Test(expected = NumericException.class)
    public void testEmptyLong() throws Exception {
        Numbers.parseLong("L");
    }

    @Test
    public void testFormatByte() {
        for (int i = 0; i < 1000; i++) {
            byte n = (byte) rnd.nextInt();

            sink.clear();
            Numbers.append(sink, n);
            Assert.assertEquals(Byte.toString(n), sink.toString());
        }
    }

    @Test
    public void testFormatChar() {
        for (int i = 0; i < 1000; i++) {
            char n = (char) rnd.nextInt();

            sink.clear();
            Numbers.append(sink, n);
            Assert.assertEquals(Integer.toString(n), sink.toString());
        }
    }


    @Test
    public void testFormatFloat() {
        Numbers.append(sink, Float.POSITIVE_INFINITY, 3);
        Assert.assertEquals(Float.toString(Float.POSITIVE_INFINITY), sink.toString());

        sink.clear();
        Numbers.append(sink, Float.NEGATIVE_INFINITY, 3);
        Assert.assertEquals(Float.toString(Float.NEGATIVE_INFINITY), sink.toString());

        sink.clear();
        Numbers.append(sink, Float.NaN, 3);
        Assert.assertEquals(Float.toString(Float.NaN), sink.toString());

        for (int i = 0; i < 1000; i++) {
            int n = rnd.nextPositiveInt() % 10;
            float f = rnd.nextFloat() * (float) Math.pow(10, n);
            sink.clear();
            Numbers.append(sink, f, 8);
            String actual = sink.toString();
            String expected = Float.toString(f);
            Assert.assertEquals(Float.parseFloat(expected), Float.parseFloat(actual), 0.00001);
        }
    }

    @Test
    public void testFormatInt() {
        for (int i = 0; i < 1000; i++) {
            int n = rnd.nextInt();
            sink.clear();
            Numbers.append(sink, n);
            Assert.assertEquals(Integer.toString(n), sink.toString());
        }
    }

    @Test
    public void testFormatLong() {
        for (int i = 0; i < 1000; i++) {
            long n = rnd.nextLong();
            sink.clear();
            Numbers.append(sink, n);
            Assert.assertEquals(Long.toString(n), sink.toString());
        }
    }

    @Test
    public void testFormatShort() {
        for (int i = 0; i < 1000; i++) {
            short n = (short) rnd.nextInt();

            sink.clear();
            Numbers.append(sink, n);
            Assert.assertEquals(Short.toString(n), sink.toString());
        }
    }


    @Test
    public void testLong() throws Exception {
        Rnd rnd = new Rnd();
        StringSink sink = new StringSink();
        for (int i = 0; i < 100; i++) {
            long l1 = rnd.nextLong();
            long l2 = rnd.nextLong();
            sink.clear();

            Numbers.append(sink, l1);
            int p = sink.length();
            Numbers.append(sink, l2);
            Assert.assertEquals(l1, Numbers.parseLong(sink, 0, p));
            Assert.assertEquals(l2, Numbers.parseLong(sink, p, sink.length()));
        }
    }

    @Test
    public void testLongToHex() {
        long value = -8372462554923253491L;
        StringSink sink = new StringSink();
        Numbers.appendHex(sink, value, false);
        TestUtils.assertEquals(Long.toHexString(value), sink);
    }

    @Test
    public void testLongToHex2() {
        long value = 0x5374f5fbcef4819L;
        StringSink sink = new StringSink();
        Numbers.appendHex(sink, value, false);
        TestUtils.assertEquals("0" + Long.toHexString(value), sink);
    }

    @Test
    public void testLongToHex3() {
        long value = 0xbfbca5da8f0645L;
        StringSink sink = new StringSink();
        Numbers.appendHex(sink, value, false);
        TestUtils.assertEquals(Long.toHexString(value), sink);
    }

    @Test
    public void testLongToString() {
        Numbers.append(sink, 6103390276L);
        TestUtils.assertEquals("6103390276", sink);
    }

    @Test(expected = NumericException.class)
    public void testParse000Greedy0() throws NumericException {
        Numbers.parseInt000Greedy("", 0, 0);
    }

    @Test
    public void testParse000Greedy1() throws NumericException {
        String input = "2";
        long val = Numbers.parseInt000Greedy(input, 0, input.length());
        Assert.assertEquals(input.length(), Numbers.decodeHighInt(val));
        Assert.assertEquals(200, Numbers.decodeLowInt(val));
    }

    @Test
    public void testParse000Greedy2() throws NumericException {
        String input = "06";
        long val = Numbers.parseInt000Greedy(input, 0, input.length());
        Assert.assertEquals(input.length(), Numbers.decodeHighInt(val));
        Assert.assertEquals(60, Numbers.decodeLowInt(val));
    }

    @Test
    public void testParse000Greedy3() throws NumericException {
        String input = "219";
        long val = Numbers.parseInt000Greedy(input, 0, input.length());
        Assert.assertEquals(input.length(), Numbers.decodeHighInt(val));
        Assert.assertEquals(219, Numbers.decodeLowInt(val));
    }

    @Test(expected = NumericException.class)
    public void testParse000Greedy4() throws NumericException {
        Numbers.parseInt000Greedy("1234", 0, 4);
    }

    @Test
    public void testParseInt() throws Exception {
        Assert.assertEquals(567963, Numbers.parseInt("567963"));
        Assert.assertEquals(-23346346, Numbers.parseInt("-23346346"));
        Assert.assertEquals(567963, Numbers.parseInt(new Utf8String("567963")));
        Assert.assertEquals(-23346346, Numbers.parseInt(new Utf8String("-23346346")));
    }

    @Test(expected = NumericException.class)
    public void testParseIntEmpty() throws Exception {
        Numbers.parseInt("");
    }

    @Test(expected = NumericException.class)
    public void testParseIntNull() throws Exception {
        Numbers.parseInt((CharSequence) null);
    }

    @Test(expected = NumericException.class)
    public void testParseIntOverflow1() throws Exception {
        String i1 = "12345566787";
        Numbers.parseInt(i1);
    }

    @Test(expected = NumericException.class)
    public void testParseIntOverflow2() throws Exception {
        Numbers.parseInt("2147483648");
    }

    @Test(expected = NumericException.class)
    public void testParseIntOverflow3() throws Exception {
        Numbers.parseInt("5000000000");
    }

    @Test(expected = NumericException.class)
    public void testParseIntSignOnly() throws Exception {
        Numbers.parseInt("-");
    }

    @Test
    public void testParseIntToDelim() throws Exception {
        String in = "1234x5";
        long val = Numbers.parseIntSafely(in, 0, in.length());
        Assert.assertEquals(1234, Numbers.decodeLowInt(val));
        Assert.assertEquals(4, Numbers.decodeHighInt(val));
    }

    @Test(expected = NumericException.class)
    public void testParseIntToDelimEmpty() throws Exception {
        String in = "x";
        Numbers.parseIntSafely(in, 0, in.length());
    }

    @Test
    public void testParseIntToDelimNoChar() throws Exception {
        String in = "12345";
        long val = Numbers.parseIntSafely(in, 0, in.length());
        Assert.assertEquals(12345, Numbers.decodeLowInt(val));
        Assert.assertEquals(5, Numbers.decodeHighInt(val));
    }

    @Test(expected = NumericException.class)
    public void testParseIntWrongChars() throws Exception {
        Numbers.parseInt("123ab");
    }

    @Test(expected = NumericException.class)
    public void testParseLongEmpty() throws Exception {
        Numbers.parseLong("");
    }

    @Test(expected = NumericException.class)
    public void testParseLongNullCharSequence() throws Exception {
        Numbers.parseLong((CharSequence) null);
    }

    @Test(expected = NumericException.class)
    public void testParseLongNullCharSequence2() throws Exception {
        Numbers.parseLong((CharSequence) null, 0, 10);
    }

    @Test(expected = NumericException.class)
    public void testParseLongOverflow1() throws Exception {
        String i1 = "1234556678723234234234234234234";
        Numbers.parseLong(i1);
    }

    @Test(expected = NumericException.class)
    public void testParseLongOverflow2() throws Exception {
        Numbers.parseLong("9223372036854775808");
    }

    @Test(expected = NumericException.class)
    public void testParseLongSignOnly() throws Exception {
        Numbers.parseLong("-");
    }

    @Test
    public void testParseLongUnderscore() throws NumericException {
        Assert.assertEquals(123_000, Numbers.parseLong("123_000"));
        Assert.assertEquals(123_343_123, Numbers.parseLong("123_343_123"));
        assertParseLongException("_889");
        assertParseLongException("__8289");
        assertParseLongException("8289_");
        assertParseLongException("8289__");
        assertParseLongException("82__89");
    }

    @Test(expected = NumericException.class)
    public void testParseLongWrongChars() throws Exception {
        Numbers.parseLong("123ab");
    }

    @FunctionalInterface
    interface ExceptionalRunnable {
        void run() throws Exception;
    }
}
