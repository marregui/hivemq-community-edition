package com.hivemq.tk;

public interface BufferWindowCharSequence extends CharSequence {
    void shiftLo(int positiveOffset);
}
