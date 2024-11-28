package com.hivemq.tk.log;

import com.hivemq.tk.str.Chars;
import com.hivemq.tk.Files;
import com.hivemq.tk.Job;
import com.hivemq.tk.NumericException;
import com.hivemq.tk.Numbers;
import com.hivemq.tk.Path;
import com.hivemq.tk.Unsafe;
import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.SingleConsumerSeq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.function.Consumer;

public class LogFileWriter extends LogWriter {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private long _wptr;
    private long buf;
    private int bufSize;
    private @Nullable String bufferSize;
    private long lim;
    private @Nullable String location;
    private @NotNull Consumer<LogRecordUtf8Sink> myConsumer = this::copyToBuffer;
    @SuppressWarnings("unused")
    private @Nullable String truncate;

    public LogFileWriter(
            final @NotNull RingQueue<LogRecordUtf8Sink> ring,
            final @NotNull SingleConsumerSeq subSeq,
            final int level) {
        super(ring, subSeq, level);
        fd = -1;
    }

    public boolean run(final int workerId) {
        return run(workerId, Job.RUNNING_STATUS);
    }

    private void copyToBuffer(final @NotNull LogRecordUtf8Sink sink) {
        final int size = sink.size();
        if ((sink.getLevel() & level) != 0 && size > 0) {
            if (_wptr + size >= lim) {
                flush();
            }

            Files.memcpy(_wptr, sink.ptr(), size);
            _wptr += size;
        }
    }

    @Override
    public void bindProperties() {
        if (bufferSize != null) {
            try {
                bufSize = Numbers.parseIntSize(bufferSize);
            } catch (final NumericException e) {
                throw new LogError("Invalid value for bufferSize");
            }
        } else {
            bufSize = DEFAULT_BUFFER_SIZE;
        }
        buf = _wptr = Unsafe.malloc(bufSize);
        lim = buf + bufSize;
        try (final Path path = new Path()) {
            path.of(location);
            if (truncate != null && Chars.equalsLowerCaseAscii(truncate, "true")) {
                fd = Files.openRW(path.$());
                Files.truncate(fd, 0);
            } else {
                fd = Files.openAppend(path.$());
            }
        }
        if (fd == -1) {
            throw new LogError("Cannot open file for append: " + location + " [errno=" + -1 + ']');
        }
    }

    @Override
    public void close() {
        if (buf != 0) {
            if (_wptr > buf) {
                flush();
            }
            Unsafe.free(buf);
            buf = 0;
        }
        if (fd != -1) {
            Files.close(fd);
            fd = -1;
        }
    }

    public int getBufSize() {
        return bufSize;
    }

    @TestOnly
    public @NotNull Consumer<LogRecordUtf8Sink> getMyConsumer() {
        return myConsumer;
    }

    @TestOnly
    public void setMyConsumer(final @NotNull Consumer<LogRecordUtf8Sink> myConsumer) {
        this.myConsumer = myConsumer;
    }

    @Override
    public boolean runSerially() {
        if (subSeq.consumeAll(ring, myConsumer)) {
            return true;
        }
        if (_wptr > buf) {
            flush();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void setBufferSize(final @Nullable String bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setLocation(final @Nullable String location) {
        this.location = location;
    }

    private void flush() {
        Files.append(fd, buf, (int) (_wptr - buf));
        _wptr = buf;
    }
}
