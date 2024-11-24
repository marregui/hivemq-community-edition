package com.hivemq.tk;

public interface WaitStrategy {
    boolean acceptSignal();

    void alert();

    void await();

    void signal();
}
