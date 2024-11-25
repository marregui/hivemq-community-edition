package com.hivemq.tk.seq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OpenBarrier implements Barrier {
    public static final OpenBarrier INSTANCE = new OpenBarrier();

    private OpenBarrier() {
    }

    @Override
    public long availableIndex(final long lo) {
        return Long.MAX_VALUE - 1;
    }

    @Override
    public long current() {
        return -1;
    }

    @Override
    public @NotNull WaitStrategy getWaitStrategy() {
        return NullWaitStrategy.INSTANCE;
    }

    @Override
    public @NotNull Barrier root() {
        return this;
    }

    @Override
    public void setBarrier(final @NotNull Barrier barrier) {
    }

    @Override
    public void setCurrent(final long value) {
        // ignored
    }

    @Override
    public @Nullable Barrier then(final @NotNull Barrier barrier) {
        return null;
    }
}
