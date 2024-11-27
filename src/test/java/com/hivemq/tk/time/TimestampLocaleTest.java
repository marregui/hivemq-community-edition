

package com.hivemq.tk.time;

import com.hivemq.tk.Numbers;
import com.hivemq.tk.NumericException;
import org.junit.Assert;
import org.junit.Test;

public class TimestampLocaleTest {
    @Test(expected = NumericException.class)
    public void testBadMonth() throws Exception {
        String date = "23 Dek 2010";
        DateLocaleFactory.INSTANCE.getLocale("en-GB").matchMonth(date, 3, date.length());
    }

    @Test(expected = NumericException.class)
    public void testBadMonth2() throws Exception {
        String date = "23 Zek 2010";
        DateLocaleFactory.INSTANCE.getLocale("en-GB").matchMonth(date, 3, date.length());
    }

    @Test
    public void testLongMonth() throws Exception {
        String date = "23 December 2010";
        long result = DateLocaleFactory.INSTANCE.getLocale("en-GB").matchMonth(date, 3, date.length());
        Assert.assertEquals(8, Numbers.decodeHighInt(result));
        Assert.assertEquals(11, Numbers.decodeLowInt(result));
    }

    @Test
    public void testLowCaseLongMonth() throws Exception {
        String date = "23 december 2010";
        long result = DateLocaleFactory.INSTANCE.getLocale("en-GB").matchMonth(date, 3, date.length());
        Assert.assertEquals(8, Numbers.decodeHighInt(result));
        Assert.assertEquals(11, Numbers.decodeLowInt(result));
    }

    @Test
    public void testRTLMonth() throws Exception {
        String s = "23مارس";
        long result = DateLocaleFactory.INSTANCE.getLocale("ar-DZ").matchMonth(s, 2, s.length());
        Assert.assertEquals(4, Numbers.decodeHighInt(result));
        Assert.assertEquals(2, Numbers.decodeLowInt(result));
    }

    @Test
    public void testShortMonth() throws Exception {
        String date = "23 Aug 2010";
        long result = DateLocaleFactory.INSTANCE.getLocale("en-GB").matchMonth(date, 3, date.length());
        Assert.assertEquals(3, Numbers.decodeHighInt(result));
        Assert.assertEquals(7, Numbers.decodeLowInt(result));
    }

    @Test(expected = NumericException.class)
    public void testWrongLength() throws Exception {
        String date = "23 Zek 2010";
        DateLocaleFactory.INSTANCE.getLocale("en-GB").matchMonth(date, 30, date.length());
    }
}
