

package com.hivemq.tk.seq;

@FunctionalInterface
public interface QueueConsumer<T> {
    void consume(T slot);
}
