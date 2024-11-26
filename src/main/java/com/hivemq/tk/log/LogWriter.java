package com.hivemq.tk.log;

import com.hivemq.tk.Files;
import com.hivemq.tk.Job;
import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.SingleConsumerSeq;
import com.hivemq.tk.Unsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

public class LogWriter implements Job, Closeable {
    protected static final long LOCKED_OFFSET = Unsafe.fieldOffset(LogWriter.class, "locked");
    protected long fd;
    protected final int level;
    protected final @NotNull RingQueue<LogRecordUtf8Sink> ring;
    protected final @NotNull SingleConsumerSeq subSeq;
    protected volatile int locked;
    protected @Nullable LogInterceptor interceptor;

    public LogWriter(
            final @NotNull RingQueue<LogRecordUtf8Sink> ring,
            final @NotNull SingleConsumerSeq subSeq,
            final int level) {
        this.ring = ring;
        this.subSeq = subSeq;
        this.level = level;
        this.fd = Files.getStdOutFdInternal();
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public boolean run(final int workerId, final @NotNull RunStatus runStatus) {
        if (Unsafe.UNSAFE.compareAndSwapInt(this, LOCKED_OFFSET, 0, 1)) {
            try {
                return runSerially();
            } finally {
                locked = 0;
            }
        }
        return false;
    }

    protected boolean runSerially() {
        return subSeq.consumeAll(ring, this::toStdOut);
    }

    public void setInterceptor(final @Nullable LogInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    private void toStdOut(final @NotNull LogRecordUtf8Sink sink) {
        if ((sink.getLevel() & this.level) != 0) {
            if (interceptor != null) {
                interceptor.onLog(sink);
            }
            Files.append(fd, sink.ptr(), sink.size());
        }
    }

    @FunctionalInterface
    public interface LogInterceptor {
        void onLog(final @NotNull LogRecordUtf8Sink sink);
    }
}
