package com.hivemq.tk.seq;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
class RhsValue extends Value {
    protected long p9, p10, p11, p12, p13, p14;

    public RhsValue(final @Nullable WaitStrategy waitStrategy) {
        super(waitStrategy);
    }
}
