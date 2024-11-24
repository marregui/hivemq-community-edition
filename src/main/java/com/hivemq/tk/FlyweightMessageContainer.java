package com.hivemq.tk;

@FunctionalInterface
public interface FlyweightMessageContainer {
    CharSequence getFlyweightMessage();

    default int getPosition() {
        return 0;
    }
}
