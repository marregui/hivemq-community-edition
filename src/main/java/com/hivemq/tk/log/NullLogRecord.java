package com.hivemq.tk.log;

import com.hivemq.tk.DirectUtf8Sequence;
import com.hivemq.tk.Sinkable;
import com.hivemq.tk.Utf8Sequence;
import com.hivemq.tk.Utf8Sink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

final class NullLogRecord implements LogRecord {

    public static final NullLogRecord INSTANCE = new NullLogRecord();

    private NullLogRecord() {
    }

    @Override
    public void $() {
    }

    @Override
    public LogRecord $(@Nullable CharSequence sequence) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable Utf8Sequence sequence) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable DirectUtf8Sequence sequence) {
        return this;
    }

    @Override
    public LogRecord $substr(int from, @Nullable DirectUtf8Sequence sequence) {
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
    public LogRecord $(double x) {
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
    public LogRecord $uuid(long lo, long hi) {
        return this;
    }

    @Override
    public LogRecord $(@Nullable Sinkable x) {
        return this;
    }

    @Override
    public LogRecord $256(long a, long b, long c, long d) {
        return this;
    }

    @Override
    public LogRecord $hex(long value) {
        return this;
    }

    @Override
    public LogRecord $hexPadded(long value) {
        return this;
    }

    @Override
    public LogRecord $ip(long ip) {
        return this;
    }

    @Override
    public LogRecord $size(long memoryBytes) {
        return this;
    }

    @Override
    public LogRecord $ts(long x) {
        return this;
    }

    @Override
    public LogRecord $utf8(long lo, long hi) {
        return this;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public LogRecord microTime(long x) {
        return this;
    }

    @Override
    public Utf8Sink put(@Nullable Utf8Sequence us) {
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
}
