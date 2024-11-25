package com.hivemq.tk.log;

import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.SingleConsumerSeq;

@FunctionalInterface
public interface LogWriterFactory {
    LogWriter createLogWriter(RingQueue<LogRecordUtf8Sink> ring, SingleConsumerSeq seq, int level);
}
