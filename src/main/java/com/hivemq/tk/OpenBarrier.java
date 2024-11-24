package com.hivemq.tk;

public final class OpenBarrier implements Barrier {
    public static final OpenBarrier INSTANCE = new OpenBarrier();

    private OpenBarrier() {
    }

    @Override
    public long availableIndex(long lo) {
        return Long.MAX_VALUE - 1;
    }

    @Override
    public long current() {
        return -1;
    }

    @Override
    public WaitStrategy getWaitStrategy() {
        return NullWaitStrategy.INSTANCE;
    }

    @Override
    public Barrier root() {
        return this;
    }

    @Override
    public void setBarrier(Barrier barrier) {
    }

    @Override
    public void setCurrent(long value) {
        // ignored
    }

    @Override
    public Barrier then(Barrier barrier) {
        return null;
    }
}
