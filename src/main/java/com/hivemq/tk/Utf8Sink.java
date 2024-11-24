package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Utf8Sink extends CharSink<Utf8Sink> {
    @Override
    default int getEncoding() {
        return CharSinkEncoding.UTF8;
    }

    @NotNull Utf8Sink put(final byte b);

    @Override
    default @NotNull Utf8Sink put(final @Nullable CharSequence cs) {
        if (cs != null) {
            put(cs, 0, cs.length());
        }
        return this;
    }

    @Override
    default @NotNull Utf8Sink put(final char c) {
        if (c < 128) {
            putAscii(c);
        } else if (c < 2048) {
            put((byte) (192 | c >> 6)).put((byte) (128 | c & 63));
        } else if (Character.isSurrogate(c)) {
            putAscii('?');
        } else {
            put((byte) (224 | c >> 12)).put((byte) (128 | c >> 6 & 63)).put((byte) (128 | c & 63));
        }
        return this;
    }

    default @NotNull Utf8Sink put(final @Nullable DirectUtf8Sequence dus) {
        if (dus != null) {
            putNonAscii(dus.lo(), dus.hi());
        }
        return this;
    }

    default @NotNull Utf8Sink put(final Utf8Sequence seq, final int lo, final int hi) {
        if (seq != null) {
            if (seq.isAscii()) {
                putAscii(seq.asAsciiCharSequence(), lo, hi);
            } else {
                for (int i = lo; i < hi; i++) {
                    putAny(seq.byteAt(i));
                }
            }
        }
        return this;
    }

    @Override
    default @NotNull Utf8Sink put(final @NotNull CharSequence cs, final int lo, final int hi) {
        int i = lo;
        while (i < hi) {
            final char c = cs.charAt(i++);
            if (c < 128) {
                putAscii(c);
            } else {
                i = Utf8s.encodeUtf16Char(this, cs, hi, i, c);
            }
        }
        return this;
    }

    default @NotNull Utf8Sink putAny(final byte b) {
        return put(b);
    }

    default @NotNull Utf8Sink putAny(final Utf8Sequence seq, final int lo, final int hi) {
        for (int i = lo; i < hi; i++) {
            putAny(seq.byteAt(i));
        }
        return this;
    }

    @Override
    default @NotNull Utf8Sink putAscii(final char c) {
        return put((byte) c);
    }

    @Override
    default @NotNull Utf8Sink putAscii(final @Nullable CharSequence cs) {
        if (cs == null) {
            return this;
        }
        final int l = cs.length();
        for (int i = 0; i < l; i++) {
            putAscii(cs.charAt(i));
        }
        return this;
    }
}
