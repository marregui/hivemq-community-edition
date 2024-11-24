package com.hivemq.tk;

/**
 * A contiguous chunk of native memory.
 */
public interface DirectSequence {

    /**
     * Address one past the last byte.
     */
    default long hi() {
        return ptr() + size();
    }

    /**
     * Address of the first byte.
     */
    default long lo() {
        return ptr();
    }

    /**
     * Address of the first character.
     */
    long ptr();

    /**
     * Number of bytes in the sequence.
     */
    int size();
}
