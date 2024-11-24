package com.hivemq.tk;

@FunctionalInterface
public interface LogWriterFactory {
    LogWriter createLogWriter(RingQueue<LogRecordUtf8Sink> ring, SCSequence seq, int level);
}
