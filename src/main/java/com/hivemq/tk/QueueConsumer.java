package com.hivemq.tk;

@FunctionalInterface
public interface QueueConsumer<T> {
    void consume(T slot);
}
