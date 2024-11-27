

package com.hivemq.tk.ds;

import com.hivemq.tk.Rnd;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class ObjIntHashMapTest {

    @Test
    public void testAddAndIterate() {
        Rnd rnd = new Rnd();

        ObjIntHashMap<String> map = new ObjIntHashMap<>();
        HashMap<String, Integer> master = new HashMap<>();

        int n = 1000;
        for (int i = 0; i < n; i++) {
            String s = rnd.nextString(rnd.nextPositiveInt() % 20);
            int v = rnd.nextInt();
            map.put(s, v);
            master.put(s, v);
        }

        for (ObjIntHashMap.Entry<String> e : map) {
            Integer val = master.get(e.key);
            Assert.assertNotNull(val);
            Assert.assertEquals(e.value, val.intValue());
        }
    }

    @Test
    public void testClassBehaviour() {
        ObjIntHashMap<Class<?>> map = new ObjIntHashMap<>();
        Assert.assertEquals(-1, map.get(Object.class));
    }
}
