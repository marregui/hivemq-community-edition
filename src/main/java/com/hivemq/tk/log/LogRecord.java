package com.hivemq.tk.log;

import com.hivemq.tk.str.NativeChunk;
import com.hivemq.tk.str.Sinkable;
import com.hivemq.tk.str.Utf8Sink;
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

    default void I$() {
        $(']').$();
    }

    LogRecord ts();

    LogRecord utf8(@Nullable CharSequence sequence);

    LogRecord $utf8(long lo, long hi);
}
