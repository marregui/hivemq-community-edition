

package com.hivemq.tk.ds;

import com.hivemq.tk.Mutable;
import com.hivemq.tk.ObjectPool;
import com.hivemq.tk.Rnd;
import com.hivemq.tk.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ObjectPoolTest {
    private static final int INITIAL_SIZE = 4;
    private ObjectPool<TestObject> pool;
    private Rnd rnd;

    @Before
    public void setUp() {
        pool = new ObjectPool<>(TestObject::new, INITIAL_SIZE);
        rnd = TestUtils.generateRandom(null);
    }

    @Test
    public void testBorrowAfterClear() {
        TestObject obj1 = pool.next();
        pool.release(obj1);
        pool.clear();

        TestObject obj2 = pool.next();
        Assert.assertNotNull("Should be able to borrow after clear", obj2);
    }

    @Test
    public void testGrowing() {
        TestObject[] objects = new TestObject[INITIAL_SIZE * 2];

        // borrow more than initial size
        for (int i = 0; i < objects.length; i++) {
            objects[i] = pool.next();
            Assert.assertNotNull("Should get object on borrow", objects[i]);
        }

        for (TestObject obj : objects) {
            pool.release(obj);
        }

        TestObject[] objectsB = new TestObject[INITIAL_SIZE * 2];
        for (int i = 0; i < objects.length; i++) {
            objectsB[i] = pool.next();
            Assert.assertNotNull("Should get object on borrow", objects[i]);
        }

        Arrays.sort(objects, Comparator.comparingInt(System::identityHashCode));
        Arrays.sort(objectsB, Comparator.comparingInt(System::identityHashCode));

        for (int i = 0; i < objects.length; i++) {
            Assert.assertSame("Should get same object", objects[i], objectsB[i]);
        }
    }

    @Test
    public void testNextReturnsNewObject() {
        TestObject obj = pool.next();
        Assert.assertNotNull("Pool should return non-null object", obj);
    }

    @Test
    public void testObjectIdentityPreserved() {
        TestObject obj1 = pool.next();
        TestObject obj2 = pool.next();
        pool.release(obj1);

        TestObject obj3 = pool.next();
        Assert.assertTrue("Should get same object instance back", obj1 == obj3 || obj2 == obj3);
    }

    @Test
    public void testObjectStateAfterReturn() {
        TestObject obj = pool.next();
        obj.setValue(42);
        pool.release(obj);

        TestObject obj2 = pool.next();
        Assert.assertSame("Should get same object back", obj, obj2);
        Assert.assertEquals("Object should be cleared after return", 0, obj.getValue());
        Assert.assertTrue("Object clear should have been called", obj.wasCleared());
    }


    @Test
    public void testReturnInDifferentOrder() {
        int size = rnd.nextInt(INITIAL_SIZE * 10) + 1;

        List<TestObject> objectsA = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            objectsA.add(pool.next());
        }

        // return in random order
        rnd.shuffle(objectsA);
        for (TestObject obj : objectsA) {
            pool.release(obj);
        }

        List<TestObject> objectsB = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            objectsB.add(pool.next());
        }

        objectsA.sort(Comparator.comparingInt(System::identityHashCode));
        objectsB.sort(Comparator.comparingInt(System::identityHashCode));

        for (int i = 0; i < size; i++) {
            Assert.assertSame("Should get same object back", objectsA.get(i), objectsB.get(i));
        }
    }

    @Test
    public void testreleaseNotFromPool() {
        TestObject notFromPool = new TestObject();
        try {
            pool.release(notFromPool);
            Assert.fail();
        } catch (AssertionError expected) {

        }

        TestObject o = pool.next();
        try {
            pool.release(notFromPool);
            Assert.fail();
        } catch (AssertionError expected) {

        }
    }

    private static class TestObject implements Mutable {
        private boolean isCleared = false;
        private int value = 0;

        @Override
        public void clear() {
            value = 0;
            isCleared = true;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public boolean wasCleared() {
            return isCleared;
        }
    }
}
