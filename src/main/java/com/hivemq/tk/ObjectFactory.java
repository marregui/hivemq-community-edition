package com.hivemq.tk;

@FunctionalInterface
public interface ObjectFactory<T> {
    T newInstance();
}
