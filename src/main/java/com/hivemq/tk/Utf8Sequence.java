package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public interface Utf8Sequence extends ByteSequence {

    /**
     * Returns a CharSequence view of the sequence.
     * The view will be only valid if the sequence contains ASCII chars only.
     *
     * @return CharSequence view of the sequence
     */
    @NotNull
    CharSequence asAsciiCharSequence();

    /**
     * Returns byte at index.
     * Note: Unchecked bounds.
     *
     * @param index byte index
     * @return byte at index
     */
    byte byteAt(int index);

    /**
     * Returns `true` if it's guaranteed that the contents of this UTF-8 sequence are
     * all ASCII characters. Returning `false` does not guarantee anything.
     */
    default boolean isAscii() {
        return false;
    }

    /**
     * Returns eight bytes of the UTF-8 sequence located at the provided
     * byte offset, packed into a single `long` value. The bytes are arranged
     * in little-endian order. The method does not check bounds and will
     * load the memory from any provided offset.
     *
     * @param offset offset of the first byte to load
     * @return byte at offset
     */
    default long longAt(int offset) {
        long result = 0;
        for (int i = offset; i < offset + Long.BYTES; i++) {
            result |= (byteAt(i) & 0xffL) << (8 * (i - offset));
        }
        return result;
    }

    /**
     * For off-heap sequences returns address of the first character.
     * For on-heap sequences returns -1.
     */
    default long ptr() {
        return -1;
    }

    /**
     * Number of bytes in the string.
     * <p>
     * This is NOT the number of 16-bit chars or code points in the string.
     * This is named `size` instead of `length` to avoid collision withs the `CharSequence` interface.
     */
    int size();
}
