package com.hivemq.tk;

/**
 * Read-only interface for a sequence of bytes with native ptr access.
 */
public interface DirectByteSequence extends ByteSequence, DirectSequence {

    /**
     * Returns byte at index.
     * Note: Unchecked bounds.
     *
     * @param index byte index
     * @return byte at index
     */
    @Override
    default byte byteAt(int index) {
        return Unsafe.UNSAFE.getByte(ptr() + index);
    }
}
