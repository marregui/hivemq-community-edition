package com.hivemq.tk.log;

import com.hivemq.tk.RingQueue;
import com.hivemq.tk.SCSequence;

@FunctionalInterface
public interface LogWriterFactory {
    LogWriter createLogWriter(RingQueue<LogRecordUtf8Sink> ring, SCSequence seq, int level);
}
