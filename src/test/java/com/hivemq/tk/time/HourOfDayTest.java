

package com.hivemq.tk.time;

import org.junit.Assert;
import org.junit.Test;

public class HourOfDayTest {

    @Test
    public void testSimple() {
        Assert.assertEquals(19, Timestamps.getHourOfDay(1592078287051004L));
        Assert.assertEquals(15, Timestamps.getHourOfDay(1592063943181693L));
        Assert.assertEquals(8, Timestamps.getHourOfDay(-1592063943181693L));
    }
}
