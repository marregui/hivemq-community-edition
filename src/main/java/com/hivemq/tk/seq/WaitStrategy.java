package com.hivemq.tk.seq;

public interface WaitStrategy {
    boolean acceptSignal();

    void alert();

    void await();

    void signal();
}
