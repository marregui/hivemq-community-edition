package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;

@SuppressWarnings("unchecked")
public interface CharSink<T extends CharSink<?>> {

    default void fillAscii(final char c, final int n) {
        for (int i = 0; i < n; i++) {
            putAscii(c);
        }
    }

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

    /**
     * Appends a UTF-8-encoded sequence to this sink.
     * <br>
     * For impls that care about the distinction between ASCII and non-ASCII:
     * If the sequence's `isAscii` status is false, this sink's `isAscii`
     * status drops to false as well.
     */
    T put(final @Nullable NativeChunk us);

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(final int value) {
        Numbers.append(this, value);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(final long value) {
        Numbers.append(this, value);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied boolean to this sink.
     */
    default T put(boolean value) {
        return putAscii(value ? "true" : "false");
    }

    /**
     * Appends an ASCII char to this sink. If the char is non-ASCII, it may append a
     * corrupted char, depending on the implementation.
     */
    T putAscii(char c);

    /**
     * Appends a sequence of ASCII chars to this sink. If some chars are non-ASCII,
     * it may append corrupted chars, depending on the implementation.
     */
    T putAscii(@Nullable CharSequence cs);

    /**
     * Appends a range of ASCII chars from the supplied array. If some chars are
     * non-ASCII, it may append corrupted chars, depending on the implementation.
     */
    default T putAscii(char @NotNull [] chars, int start, int len) {
        for (int i = 0; i < len; i++) {
            putAscii(chars[i + start]);
        }
        return (T) this;
    }

    /**
     * Appends a range of ASCII chars from the supplied sequence to this sink.
     * If some chars are non-ASCII, it may append corrupted chars, depending on
     * the implementation.
     */
    default T putAscii(@NotNull CharSequence cs, int start, int len) {
        for (int i = start; i < len; i++) {
            putAscii(cs.charAt(i));
        }
        return (T) this;
    }

    default T putEOL() {
        return putAscii(Misc.EOL);
    }

    default T putISODate(long micros) {
        putAscii(Files.microsToStr(micros));
        return (T) this;
    }
    //"yyyy-MM-ddTHH:mm:ss.SSSUUUz";

    /**
     * Accepts a range of memory addresses from lo to hi (exclusive), expecting it to
     * point to a block of valid UTF-8 bytes, and appends it to this sink.
     * <br>
     * For impls that care about the distinction between ASCII and non-ASCII:
     * Drops the `isAscii` status of this sink.
     */
    T putNonAscii(long lo, long hi);
}
