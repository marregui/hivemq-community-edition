package com.hivemq.tk.log;

import com.hivemq.tk.str.NativeChunk;
import com.hivemq.tk.str.Sinkable;
import com.hivemq.tk.str.Utf8Sink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

final class NullLogRecord implements LogRecord {

    public static final NullLogRecord INSTANCE = new NullLogRecord();

    NullLogRecord() {
    }

    @Override
    public void $() {
    }

    @Override
    public LogRecord $(@Nullable CharSequence sequence) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable NativeChunk sequence) {
        return this;
    }

    @Override
    public LogRecord $(@NotNull CharSequence sequence, int lo, int hi) {
        return this;
    }

    @Override
    public LogRecord $(int x) {
        return this;
    }

    @Override
    public LogRecord $(long l) {
        return this;
    }

    @Override
    public LogRecord $(boolean x) {
        return this;
    }

    @Override
    public LogRecord $(char c) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable Throwable e) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable File x) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable Object x) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable Sinkable x) {
        return this;
    }

    @Override
    public Utf8Sink put(@Nullable NativeChunk us) {
        return this;
    }

    @Override
    public Utf8Sink put(byte b) {
        return this;
    }

    @Override
    public LogRecord put(char c) {
        return this;
    }

    @Override
    public Utf8Sink putNonAscii(long lo, long hi) {
        return this;
    }

    @Override
    public LogRecord ts() {
        return this;
    }

    @Override
    public LogRecord utf8(@Nullable CharSequence sequence) {
        return this;
    }

    @Override
    public LogRecord $utf8(final long lo, final long hi) {
        return this;
    }
}
