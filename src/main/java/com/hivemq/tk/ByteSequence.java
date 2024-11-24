package com.hivemq.tk;

/**
 * Read-only interface for a sequence of bytes.
 */
public interface ByteSequence {

    /**
     * Returns byte at index.
     * Note: Unchecked bounds.
     *
     * @param index byte index
     * @return byte at index
     */
    byte byteAt(int index);  // TODO: Convert to take `long` instead of `int`.

    /**
     * Number of bytes in the sequence.
     * Note that this is not called `length()` to avoid collision with `CharSequence` (and other) interfaces.
     */
    int size();  // TODO: Convert this to return `long`. Also see `byte_sink.cpp` which restricts the size to `int`.
}
