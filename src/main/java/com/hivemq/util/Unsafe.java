package com.hivemq.util;

import java.lang.reflect.Field;

public class Unsafe {
    public static final sun.misc.Unsafe UNSAFE;


    static {
        try {
            final Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) theUnsafe.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    // most significant bit
    private static int msb(int value) {
        return 31 - Integer.numberOfLeadingZeros(value);
    }

    public static long fieldOffset(Class clz, String fieldName) throws RuntimeException {
        try {
            return UNSAFE.objectFieldOffset(clz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasGetAndSetSupport() {
        try {
            sun.misc.Unsafe.class.getMethod("getAndSetObject", Object.class, Long.TYPE, Object.class);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}
