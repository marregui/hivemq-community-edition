

package com.hivemq.tk.ds;

import com.hivemq.tk.Rnd;
import com.hivemq.tk.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class IntObjHashMapTest {
    @Test
    public void testAll() {

        Rnd rnd = new Rnd();
        // populate map
        IntObjHashMap<CharSequence> map = new IntObjHashMap<>();
        final int N = 1000;
        for (int i = 0; i < N; i++) {
            CharSequence cs = rnd.nextChars(15);
            map.put(rnd.nextInt(), cs.toString());
        }
        Assert.assertEquals(N, map.size());

        rnd.reset();

        // assert that map contains the values we just added
        for (int i = 0; i < N; i++) {
            CharSequence cs = rnd.nextChars(15);
            int value = rnd.nextInt();
            Assert.assertFalse(map.excludes(value));
            Assert.assertEquals(cs, map.get(value));
        }

        Rnd rnd2 = new Rnd();

        rnd.reset();

        // remove some keys and assert that the size() complies
        int removed = 0;
        for (int i = 0; i < N; i++) {
            rnd.nextChars(15);
            int value = rnd.nextInt();
            if (rnd2.nextPositiveInt() % 16 == 0) {
                Assert.assertTrue(map.remove(value) > -1);
                removed++;
                Assert.assertEquals(N - removed, map.size());
            }
        }

        // if we didn't remove anything test has no value
        Assert.assertTrue(removed > 0);

        rnd2.reset();
        rnd.reset();

        Rnd rnd3 = new Rnd();

        // assert that keys we didn't remove are still there and
        // keys we removed are not
        for (int i = 0; i < N; i++) {
            CharSequence cs = rnd.nextChars(15);
            int value = rnd.nextInt();
            if (rnd2.nextPositiveInt() % 16 == 0) {
                Assert.assertTrue(map.excludes(value));
            } else {
                Assert.assertFalse(map.excludes(value));

                int index = map.keyIndex(value);
                TestUtils.assertEquals(cs, map.valueAt(index));

                // update value
                map.putAt(index, value, rnd3.nextChars(5));
            }
        }

        // assert that update is visible correctly

        rnd3.reset();
        rnd2.reset();
        rnd.reset();

        // assert that keys we didn't remove are still there and
        // keys we removed are not
        for (int i = 0; i < N; i++) {
            rnd.nextChars(15);
            int value = rnd.nextInt();
            if (rnd2.nextPositiveInt() % 16 == 0) {
                Assert.assertTrue(map.excludes(value));
            } else {
                Assert.assertFalse(map.excludes(value));
                TestUtils.assertEquals(rnd3.nextChars(3), map.get(value));
            }
        }
    }
}
