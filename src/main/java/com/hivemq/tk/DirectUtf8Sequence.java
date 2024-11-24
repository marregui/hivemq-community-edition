package com.hivemq.tk;

public interface DirectUtf8Sequence extends Utf8Sequence, DirectByteSequence {

    @Override
    default byte byteAt(int index) {
        return Unsafe.UNSAFE.getByte(ptr() + index);
    }

    @Override
    default long longAt(int offset) {
        return Unsafe.UNSAFE.getLong(ptr() + offset);
    }

    @Override
    long ptr();
}
