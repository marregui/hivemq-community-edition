package com.hivemq.tk;

@FunctionalInterface
public interface DirectObjectFactory<T> {
    T newInstance(final long address, long addressSize);
}
