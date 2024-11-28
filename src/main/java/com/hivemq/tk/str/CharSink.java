package com.hivemq.tk.str;

import com.hivemq.tk.Files;
import com.hivemq.tk.Numbers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public interface CharSink<T extends CharSink<?>> {

    int getEncoding();

    default @NotNull T put(final @NotNull CharSequence cs, final int lo, final int hi) {
        for (int i = lo; i < hi; i++) {
            put(cs.charAt(i));
        }
        return (T) this;
    }

    default @NotNull T put(final @Nullable Sinkable sinkable) {
        if (sinkable != null) {
            sinkable.toSink(this);
        }
        return (T) this;
    }

    T put(char c);

    default @NotNull T put(final @Nullable CharSequence cs) {
        if (cs != null) {
            for (int i = 0, n = cs.length(); i < n; i++) {
                put(cs.charAt(i));
            }
        }
        return (T) this;
    }

    T put(final @Nullable NativeChunk us);

    default T put(final int value) {
        Numbers.append(this, value);
        return (T) this;
    }

    default T put(final long value) {
        Numbers.append(this, value);
        return (T) this;
    }

    default T put(boolean value) {
        return putAscii(value ? "true" : "false");
    }

    T putAscii(char c);

    T putAscii(@Nullable CharSequence cs);

    default T putAscii(char @NotNull [] chars, int start, int len) {
        for (int i = 0; i < len; i++) {
            putAscii(chars[i + start]);
        }
        return (T) this;
    }

    default T putAscii(@NotNull CharSequence cs, int start, int len) {
        for (int i = start; i < len; i++) {
            putAscii(cs.charAt(i));
        }
        return (T) this;
    }

    default T putEOL() {
        return putAscii(Files.EOL);
    }

    default T putISODate(long micros) {
        putAscii(Files.microsToStr(micros));
        return (T) this;
    }

    T putNonAscii(long lo, long hi);
}
