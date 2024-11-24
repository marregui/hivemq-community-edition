package com.hivemq.tk;

public final class NullWaitStrategy implements WaitStrategy {
    public static final NullWaitStrategy INSTANCE = new NullWaitStrategy();

    private NullWaitStrategy() {
    }

    @Override
    public boolean acceptSignal() {
        return false;
    }

    @Override
    public void alert() {
    }

    @Override
    public void await() {
    }

    @Override
    public void signal() {
    }
}
