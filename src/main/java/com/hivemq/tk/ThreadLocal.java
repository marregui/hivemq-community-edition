

package com.hivemq.tk;

import java.io.Closeable;
import java.util.function.Supplier;

public class ThreadLocal<T> extends java.lang.ThreadLocal<T> implements Closeable {
    private final Supplier<T> factory;

    public ThreadLocal(Supplier<T> factory) {
        this.factory = factory;
    }

    @Override
    public void close() {
        Files.freeIfCloseable(super.get());
        remove();
    }

    @Override
    public T get() {
        T val = super.get();
        if (val == null) {
            val = factory.get();
            set(val);
        }
        return val;
    }
}
