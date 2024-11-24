package com.hivemq.tk;

public interface BinarySequence {

    byte byteAt(long index);

    /**
     * Copies bytes from this binary sequence to buffer.
     *
     * @param address target buffer address
     * @param start   offset in binary sequence to start copying from
     * @param length  number of bytes to copy
     */
    default void copyTo(long address, long start, long length) {
        final long n = Math.min(length() - start, length);
        for (long l = 0; l < n; l++) {
            Unsafe.UNSAFE.putByte(address + l, byteAt(start + l));
        }
    }

    long length();
}
