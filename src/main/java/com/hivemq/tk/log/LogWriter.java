package com.hivemq.tk.log;

import com.hivemq.tk.Job;
import com.hivemq.tk.QueueConsumer;
import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.SingleConsumerSeq;
import com.hivemq.tk.Unsafe;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public class LogWriter implements Job, Closeable {
    private static final long LOCKED_OFFSET = Unsafe.fieldOffset(LogWriter.class, "locked");

    private volatile int locked = 0;
    private final long fd = -1;//TODO Files.getStdOutFdInternal();
    private final int level;
    private final RingQueue<LogRecordUtf8Sink> ring;
    private final SingleConsumerSeq subSeq;
    private LogInterceptor interceptor;
    private final QueueConsumer<LogRecordUtf8Sink> myConsumer = this::toStdOut;


    public LogWriter(RingQueue<LogRecordUtf8Sink> ring, SingleConsumerSeq subSeq, int level) {
        this.ring = ring;
        this.subSeq = subSeq;
        this.level = level;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean run(final int workerId,final @NotNull RunStatus runStatus) {
        if (Unsafe.UNSAFE.compareAndSwapInt(this, LOCKED_OFFSET, 0, 1)) {
            try {
                return runSerially();
            } finally {
                locked = 0;
            }
        }
        return false;
    }

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
