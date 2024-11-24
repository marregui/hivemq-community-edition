package com.hivemq.tk.log;

import com.hivemq.tk.QueueConsumer;
import com.hivemq.tk.RingQueue;
import com.hivemq.tk.SCSequence;
import com.hivemq.tk.SynchronizedJob;

import java.io.Closeable;

public class LogConsoleWriter extends SynchronizedJob implements Closeable, LogWriter {
    private final long fd = -1;//TODO Files.getStdOutFdInternal();
    private final int level;
    private final RingQueue<LogRecordUtf8Sink> ring;
    private final SCSequence subSeq;
    private LogInterceptor interceptor;
    private final QueueConsumer<LogRecordUtf8Sink> myConsumer = this::toStdOut;


    public LogConsoleWriter(RingQueue<LogRecordUtf8Sink> ring, SCSequence subSeq, int level) {
        this.ring = ring;
        this.subSeq = subSeq;
        this.level = level;
    }

    @Override
    public void bindProperties(LogFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean runSerially() {
        return subSeq.consumeAll(ring, myConsumer);
    }

    public void setInterceptor(LogInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    private void toStdOut(LogRecordUtf8Sink sink) {
        if ((sink.getLevel() & this.level) != 0) {
            if (interceptor != null) {
                interceptor.onLog(sink);
            }
            // TODO
            //Files.append(fd, sink.ptr(), sink.size());
        }
    }

    @FunctionalInterface
    public interface LogInterceptor {
        void onLog(LogRecordUtf8Sink sink);
    }
}
