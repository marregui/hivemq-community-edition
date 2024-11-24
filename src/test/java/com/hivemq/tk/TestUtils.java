/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package com.hivemq.tk;

import com.hivemq.tk.ds.IntList;
import com.hivemq.tk.ds.LongList;
import com.hivemq.tk.ds.ObjList;
import org.junit.Assert;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;


public final class TestUtils {
    private static final ThreadLocal<StringSink> tlSink = ThreadLocal.withInitial(() -> new StringSink());

    private TestUtils() {
    }

    public static void assertEquals(CharSequence expected, Sinkable actual) {
        StringSink sink = getTlSink();
        actual.toSink(sink);
        assertEquals(null, expected, sink);
    }

    public static void assertEquals(CharSequence expected, Utf8Sequence actual) {
        StringSink sink = getTlSink();
        Utf8s.utf8ToUtf16(actual, sink);
        assertEquals(null, expected, sink);
    }

    public static void assertEquals(byte[] expected, Utf8Sequence actual) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected != null && actual == null) {
            Assert.fail("Expected: \n`" + Arrays.toString(expected) + "`but have NULL");
        }

        if (expected == null) {
            Assert.fail("Expected: NULL but have \n`" + actual + "`\n");
        }

        if (expected.length != actual.size()) {
            Assert.fail("Expected size: " + expected.length + ", but have " + actual.size());
        }

        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual.byteAt(i)) {
                Assert.fail("Expected byte: " + expected[i] + ", but have " + actual.byteAt(i) + " at index " + i);
            }
        }
    }

    public static void assertEquals(Utf8Sequence expected, Utf8Sequence actual) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected != null && actual == null) {
            Assert.fail("Expected: \n`" + expected + "`\nbut have NULL. ");
        }

        if (expected == null) {
            Assert.fail("Expected: NULL but have \n`" + actual + "`\n");
        }

        if (expected.size() != actual.size()) {
            Assert.assertEquals(expected, actual);
        }

        StringSink sink = getTlSink();
        Utf8s.utf8ToUtf16(expected, sink);
        String expectedStr = sink.toString();
        sink.clear();
        Utf8s.utf8ToUtf16(actual, sink);
        assertEquals(expectedStr, sink);
    }

    public static void assertEquals(CharSequence expected, CharSequence actual) {
        assertEquals(null, expected, actual);
    }

    public static void assertEquals(String message, CharSequence expected, CharSequence actual) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected != null && actual == null) {
            Assert.fail("Expected: \n`" + expected + "`but have NULL");
        }

        if (expected == null) {
            Assert.fail("Expected: NULL but have \n`" + actual + "`\n");
        }

        if (expected.length() != actual.length()) {
            Assert.assertEquals(message, expected, actual);
        }

        for (int i = 0; i < expected.length(); i++) {
            if (expected.charAt(i) != actual.charAt(i)) {
                Assert.assertEquals(message, expected, actual);
            }
        }
    }

    public static void assertEquals(BinarySequence bs, BinarySequence actBs, long actualLen) {
        if (bs == null) {
            Assert.assertNull(actBs);
            Assert.assertEquals(-1, actualLen);
        } else {
            Assert.assertEquals(bs.length(), actBs.length());
            Assert.assertEquals(bs.length(), actualLen);
            for (long l = 0, z = bs.length(); l < z; l++) {
                byte b1 = bs.byteAt(l);
                byte b2 = actBs.byteAt(l);
                if (b1 != b2) {
                    Assert.fail("Failed comparison at [" + l + "], expected: " + b1 + ", actual: " + b2);
                }
                Assert.assertEquals(bs.byteAt(l), actBs.byteAt(l));
            }
        }
    }

    public static void assertEquals(LongList expected, LongList actual) {
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0, n = expected.size(); i < n; i++) {
            if (expected.getQuick(i) != actual.getQuick(i)) {
                Assert.assertEquals("index " + i, expected.getQuick(i), actual.getQuick(i));
            }
        }
    }

    public static void assertEquals(IntList expected, IntList actual) {
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0, n = expected.size(); i < n; i++) {
            if (expected.getQuick(i) != actual.getQuick(i)) {
                Assert.assertEquals("index " + i, expected.getQuick(i), actual.getQuick(i));
            }
        }
    }

    public static <T> void assertEquals(ObjList<T> expected, ObjList<T> actual) {
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0, n = expected.size(); i < n; i++) {
            if (expected.getQuick(i) != actual.getQuick(i)) {
                Assert.assertEquals("index " + i, expected.getQuick(i), actual.getQuick(i));
            }
        }
    }

    public static void await(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Throwable ignore) {
        }
    }

    public static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (Throwable ignore) {
        }
    }


    public static boolean equals(CharSequence expected, CharSequence actual) {
        if (expected == null && actual == null) {
            return true;
        }

        if (expected == null || actual == null) {
            return false;
        }

        if (expected.length() != actual.length()) {
            return false;
        }

        for (int i = 0; i < expected.length(); i++) {
            if (expected.charAt(i) != actual.charAt(i)) {
                return false;
            }
        }
        return true;
    }


    public static void unchecked(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void unchecked(CheckedRunnable runnable, AtomicInteger failureCounter) {
        try {
            runnable.run();
        } catch (Throwable e) {
            failureCounter.incrementAndGet();
            throw new RuntimeException(e);
        }
    }

    public static <T> T unchecked(CheckedSupplier<T> runnable) {
        try {
            return runnable.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int unchecked(CheckedIntFunction runnable) {
        try {
            return runnable.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static StringSink getTlSink() {
        StringSink ss = tlSink.get();
        ss.clear();
        return ss;
    }

    public interface CheckedIntFunction {
        int get() throws Throwable;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Throwable;
    }

    public interface CheckedSupplier<T> {
        T get() throws Throwable;
    }
}
