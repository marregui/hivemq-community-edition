

package com.hivemq.tk.seq;

public class YieldingWaitStrategy extends AbstractWaitStrategy {
    public static void pause() {
        try {
            Thread.sleep(0);
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public boolean acceptSignal() {
        return false;
    }

    @Override
    public void await() {
        pause();
    }

    @Override
    public void signal() {
    }
}
