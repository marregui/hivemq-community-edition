package com.hivemq.tk.log;

import com.hivemq.tk.NativeChunk;
import com.hivemq.tk.Sinkable;
import com.hivemq.tk.Utf8Sink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface LogRecord extends Utf8Sink {

    void $();

    LogRecord $(@Nullable CharSequence sequence);

    LogRecord $(@Nullable NativeChunk sequence);

    LogRecord $(@NotNull CharSequence sequence, int lo, int hi);

    LogRecord $(int x);

    LogRecord $(long l);

    LogRecord $(boolean x);

    LogRecord $(char c);

    LogRecord $(@Nullable Throwable e);

    LogRecord $(@Nullable File x);

    LogRecord $(@Nullable Object x);

    LogRecord $(@Nullable Sinkable x);

    LogRecord $256(long a, long b, long c, long d);

    LogRecord $hex(long value);

    LogRecord $hexPadded(long value);

    LogRecord $ip(long ip);

    LogRecord $substr(int from, @Nullable NativeChunk sequence);

    LogRecord $ts(long x);

    LogRecord $utf8(long lo, long hi);

    LogRecord $uuid(long lo, long hi);

    default void I$() {
        $(']').$();
    }

    boolean isEnabled();

    LogRecord microTime(long x);

    LogRecord ts();

    LogRecord utf8(@Nullable CharSequence sequence);
}
