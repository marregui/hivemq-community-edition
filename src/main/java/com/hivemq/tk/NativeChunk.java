package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public interface NativeChunk {

    default long hi() {
        return ptr() + size();
    }

    default long lo() {
        return ptr();
    }

    /**
     * For off-heap sequences returns address of the first character.
     * For on-heap sequences returns -1.
     */
    default long ptr() {
        return -1;
    }

    int size();

    default byte byteAt(final int index) {
        return Unsafe.UNSAFE.getByte(ptr() + index);
    }

    default @NotNull CharSequence asAsciiCharSequence(){
        throw new UnsupportedOperationException();
    }

    default boolean isAscii() {
        return false;
    }
}
