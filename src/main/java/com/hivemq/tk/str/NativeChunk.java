package com.hivemq.tk.str;

import com.hivemq.tk.Unsafe;
import org.jetbrains.annotations.NotNull;

public interface NativeChunk {

    default long hi() {
        return ptr() + size();
    }

    default long lo() {
        return ptr();
    }

    /**
     * off-heap sequences: address of the first character
     * on-heap sequences: -1.
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
