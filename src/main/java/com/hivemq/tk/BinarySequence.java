package com.hivemq.tk;

public interface BinarySequence {

    byte byteAt(long index);

    long length();
}
