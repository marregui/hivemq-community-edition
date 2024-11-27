

package com.hivemq.tk.seq;

import static com.hivemq.tk.Files.pause;

public class YieldingWaitStrategy extends AbstractWaitStrategy {

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
