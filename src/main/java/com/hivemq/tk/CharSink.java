package com.hivemq.tk;

import com.hivemq.tk.time.TimestampFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A sink that does not expose its storage format. Users of this interface must
 * not make any assumptions about the storage format.
 */
@SuppressWarnings("unchecked")
public interface CharSink<T extends CharSink<?>> {

    /**
     * Assumes the char is ASCII and appends it to the sink n times.
     * If the char is non-ASCII, it may append a corrupted char, depending
     * on the implementation.
     */
    default void fillAscii(char c, int n) {
        for (int i = 0; i < n; i++) {
            putAscii(c);
        }
    }

    int getEncoding();

    default T put(@NotNull CharSequence cs, int lo, int hi) {
        for (int i = lo; i < hi; i++) {
            put(cs.charAt(i));
        }
        return (T) this;
    }

    default T put(@Nullable Sinkable sinkable) {
        if (sinkable != null) {
            sinkable.toSink(this);
        }
        return (T) this;
    }

    T put(char c);

    default T put(@Nullable CharSequence cs) {
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
    T put(@Nullable Utf8Sequence us);

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(int value) {
        Numbers.append(this, value);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(long value) {
        Numbers.append(this, value);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(float value) {
        Numbers.append(this, value);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(float value, int scale) {
        Numbers.append(this, value, scale);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(double value) {
        Numbers.append(this, value);
        return (T) this;
    }

    /**
     * Appends a string representation of the supplied number to this sink.
     */
    default T put(double value, int scale) {
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

    default T putISODate(long value) {
        TimestampFormatUtils.appendDateTimeUSec(this, value);
        return (T) this;
    }

    /**
     * Accepts a range of memory addresses from lo to hi (exclusive), expecting it to
     * point to a block of valid UTF-8 bytes, and appends it to this sink.
     * <br>
     * For impls that care about the distinction between ASCII and non-ASCII:
     * Drops the `isAscii` status of this sink.
     */
    T putNonAscii(long lo, long hi);

    default CharSink putSize(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? put(bytes).put(' ').put('B')
                : b <= 0xfffccccccccccccL >> 40 ? put(Math.round(bytes / 0x1p10 * 1000.0) / 1000.0).put(" KiB")
                : b <= 0xfffccccccccccccL >> 30 ? put(Math.round(bytes / 0x1p20 * 1000.0) / 1000.0).put(" MiB")
                : b <= 0xfffccccccccccccL >> 20 ? put(Math.round(bytes / 0x1p30 * 1000.0) / 1000.0).put(" GiB")
                : b <= 0xfffccccccccccccL >> 10 ? put(Math.round(bytes / 0x1p40 * 1000.0) / 1000.0).put(" TiB")
                : b <= 0xfffccccccccccccL ? put(Math.round((bytes >> 10) / 0x1p40 * 1000.0) / 1000.0).put(" PiB")
                : put(Math.round((bytes >> 20) / 0x1p40 * 1000.0) / 1000.0).put(" EiB");
    }
}
