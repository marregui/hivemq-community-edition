

package com.hivemq.tk.time;

import com.hivemq.tk.Numbers;
import org.junit.Assert;
import org.junit.Test;

public class OffsetParserTest {

    @Test
    public void testBadDelim() {
        assertError("UTC+01x30");
    }

    @Test
    public void testBadGMT() {
        assertError("GMX+03:30");
    }

    @Test
    public void testBadHour() {
        assertError("UTC+0x:30");
    }

    @Test
    public void testBadMinute() {
        assertError("UTC+01:3x");
    }

    @Test
    public void testBadSign() {
        assertError("GMT*08:00");
    }

    @Test
    public void testBadStart() {
        assertError("*08");
    }

    @Test
    public void testBadUtc() {
        assertError("UTX+09:00");
    }

    @Test
    public void testGMTCamelCasePositive() {
        assertThat(2 * 60 + 15, "gMt+02:15");
    }

    @Test
    public void testGMTNegative() {
        assertThat(-2 * 60 - 15, "gMt-02:15");
    }

    @Test
    public void testGMTPositive() {
        assertThat(3 * 60 + 30, "GMT+03:30");
    }

    @Test
    public void testHourMinNoDelim() {
        assertThat(8 * 60 + 15, "0815");
    }

    @Test
    public void testHourMinNoDelimNegative() {
        assertThat(-3 * 60 - 30, "-0330");
    }

    @Test
    public void testHourMinNoDelimPositive() {
        assertThat(8 * 60 + 15, "+0815");
    }

    @Test
    public void testHoursOnly() {
        assertThat(8 * 60, "08");
    }

    @Test
    public void testLargeHour() {
        assertError("UTC+24:00");
    }

    @Test
    public void testLargeMinute() {
        assertError("UTC+12:60");
    }

    @Test
    public void testMissingHour() {
        assertError("UTC+");
    }

    @Test
    public void testMissingMinute() {
        assertError("UTC+01:");
    }

    @Test
    public void testNegativeHoursOnly() {
        assertThat(-4 * 60, "-04");
    }

    @Test
    public void testPositiveHoursOnly() {
        assertThat(8 * 60, "+08");
    }

    @Test
    public void testShortHour() {
        assertError("UTC+1");
    }

    @Test
    public void testShortMinute() {
        assertError("UTC+01:3");
    }

    @Test
    public void testUTCCamelCasePositive() {
        assertThat(9 * 60, "uTc+09:00");
    }

    @Test
    public void testUTCNegative() {
        assertThat(-4 * 60 - 15, "UTC-04:15");
    }

    @Test
    public void testUTCPositive() {
        assertThat(9 * 60, "UTC+09:00");
    }

    private static void assertError(String offset) {
        Assert.assertEquals(Long.MIN_VALUE, Timestamps.parseOffset(offset));
    }

    private static void assertThat(int expected, String offset) {
        long r = Timestamps.parseOffset(offset);
        Assert.assertEquals(expected, Numbers.decodeLowInt(r));
    }
}
