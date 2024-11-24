package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

/**
 * A view on top of an ASCII-only {@link Utf8Sequence}.
 */
public class AsciiCharSequence implements CharSequence {
    private int len;
    private Utf8Sequence original;
    private int start;
    private AsciiCharSequence subSequence;

    @Override
    public char charAt(int i) {
        return (char) original.byteAt(i + start);
    }

    @Override
    public int length() {
        return len;
    }

    public AsciiCharSequence of(Utf8Sequence original) {
        this.original = original;
        this.start = 0;
        this.len = original.size();
        return this;
    }

    public AsciiCharSequence of(Utf8Sequence original, int start, int len) {
        this.original = original;
        this.start = start;
        this.len = len;
        return this;
    }

    @Override
    public @NotNull CharSequence subSequence(int start, int end) {
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
