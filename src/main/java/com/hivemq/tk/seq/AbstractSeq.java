package com.hivemq.tk.seq;

import com.hivemq.tk.Unsafe;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSeq extends RhsValue {
    private static final long CACHE_OFFSET = Unsafe.fieldOffset(Value.class, "cache");
    private static final long VALUE_OFFSET = Unsafe.fieldOffset(Value.class, "value");

    public AbstractSeq(final @Nullable WaitStrategy waitStrategy) {
        super(waitStrategy);
    }

    protected boolean casValue(final long expected, final long value) {
        return Unsafe.cas(this, VALUE_OFFSET, expected, value);
    }

    protected long getValue() {
        return Unsafe.UNSAFE.getLong(this, VALUE_OFFSET);
    }

    protected void setCacheFenced(final long cache) {
        Unsafe.UNSAFE.putOrderedLong(this, CACHE_OFFSET, cache);
    }
}
