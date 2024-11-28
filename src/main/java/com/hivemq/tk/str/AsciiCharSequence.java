package com.hivemq.tk.str;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class AsciiCharSequence implements CharSequence {
    private @NotNull NativeChunk original;
    private int start;
    private int len;
    private @Nullable AsciiCharSequence subSequence;

    @Override
    public char charAt(final int i) {
        return (char) original.byteAt(i + start);
    }

    @Override
    public int length() {
        return len;
    }

    public @NotNull AsciiCharSequence of(final @NotNull NativeChunk original) {
        this.original = original;
        this.start = 0;
        this.len = original.size();
        return this;
    }

    public @NotNull AsciiCharSequence of(final @NotNull NativeChunk original, final int start, final int len) {
        this.original = original;
        this.start = start;
        this.len = len;
        return this;
    }

    @Override
    public @NotNull CharSequence subSequence(final int start, final int end) {
        if (subSequence == null) {
            subSequence = new AsciiCharSequence();
        }
        return subSequence.of(original, start, end - start);
    }

    @Override
    public @NotNull String toString() {
        return original.toString();
    }
}
