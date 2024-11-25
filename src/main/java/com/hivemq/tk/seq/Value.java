package com.hivemq.tk.seq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Value extends LhsValue {
    private final @NotNull WaitStrategy waitStrategy;
    protected @NotNull Barrier barrier = OpenBarrier.INSTANCE;
    protected long cache = -1;
    protected volatile long value = -1;

    public Value(final @Nullable WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy == null ? NullWaitStrategy.INSTANCE : waitStrategy;
    }

    public @NotNull WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }
}
