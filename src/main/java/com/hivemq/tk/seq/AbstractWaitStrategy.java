

package com.hivemq.tk.seq;

abstract class AbstractWaitStrategy implements WaitStrategy {

    volatile boolean alerted = false;

    @Override
    public void alert() {
        alerted = true;
        signal();
    }
}

