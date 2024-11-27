package com.hivemq.tk.log;

import com.hivemq.tk.*;
import com.hivemq.tk.ds.ObjHashSet;
import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.Seq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.ThreadLocal;
import java.util.Set;

abstract class AbstractLogRecord implements LogRecord, Log {
    private static final ThreadLocal<ObjHashSet<Throwable>> tlSet = ThreadLocal.withInitial(ObjHashSet::new);
    protected final RingQueue<LogRecordUtf8Sink> advisoryRing;
    protected final Seq advisorySeq;
    protected final RingQueue<LogRecordUtf8Sink> criticalRing;
    protected final Seq criticalSeq;
    protected final RingQueue<LogRecordUtf8Sink> debugRing;
    protected final Seq debugSeq;
    protected final RingQueue<LogRecordUtf8Sink> errorRing;
    protected final Seq errorSeq;
    protected final RingQueue<LogRecordUtf8Sink> infoRing;
    protected final Seq infoSeq;
    protected final ThreadLocalCursor tl = new ThreadLocalCursor();
    private final CharSequence name;

    AbstractLogRecord(
            CharSequence name,
            RingQueue<LogRecordUtf8Sink> debugRing,
            Seq debugSeq,
            RingQueue<LogRecordUtf8Sink> infoRing,
            Seq infoSeq,
            RingQueue<LogRecordUtf8Sink> errorRing,
            Seq errorSeq,
            RingQueue<LogRecordUtf8Sink> criticalRing,
            Seq criticalSeq,
            RingQueue<LogRecordUtf8Sink> advisoryRing,
            Seq advisorySeq
    ) {
        this.name = name;
        this.debugRing = debugRing;
        this.debugSeq = debugSeq;
        this.infoRing = infoRing;
        this.infoSeq = infoSeq;
        this.errorRing = errorRing;
        this.errorSeq = errorSeq;
        this.criticalRing = criticalRing;
        this.criticalSeq = criticalSeq;
        this.advisoryRing = advisoryRing;
        this.advisorySeq = advisorySeq;
    }

    @Override
    public LogRecord $(int x) {
        sink().put(x);
        return this;
    }

    @Override
    public LogRecord $(@Nullable NativeChunk sequence) {
        if (sequence == null) {
            sink().putAscii("null");
        } else {
            sink().put(sequence);
        }
        return this;
    }

    @Override
    public LogRecord $(@NotNull CharSequence sequence, int lo, int hi) {
        sink().putAscii(sequence, lo, hi);
        return this;
    }

    @Override
    public LogRecord $(@Nullable File x) {
        sink().put(x == null ? "null" : x.getAbsolutePath());
        return this;
    }

    public LogRecord $(@Nullable CharSequence sequence) {
        if (sequence == null) {
            sink().putAscii("null");
        } else {
            sink().putAscii(sequence);
        }
        return this;
    }

    @Override
    public LogRecord $(@Nullable Object x) {
        if (x == null) {
            sink().putAscii("null");
        } else {
            try {
                sink().put(x.toString());
            } catch (Throwable t) {
                // Complex toString() method could throw e.g. NullPointerException.
                // If that happens, we've to release cursor to prevent blocking log queue.
                $();
                throw t;
            }
        }
        return this;
    }

    @Override
    public LogRecord $(@Nullable Sinkable x) {
        if (x == null) {
            sink().putAscii("null");
        } else {
            try {
                x.toSink(sink());
            } catch (Throwable t) {
                // Complex toSink() method could throw e.g. NullPointerException.
                // If that happens, we've to release cursor to prevent blocking log queue.
                $();
                throw t;
            }
        }
        return this;
    }

    @Override
    public LogRecord $(long l) {
        sink().put(l);
        return this;
    }

    @Override
    public LogRecord $(boolean x) {
        sink().put(x);
        return this;
    }

    @Override
    public LogRecord $(char c) {
        sink().put(c);
        return this;
    }

    @Override
    public LogRecord $(@Nullable Throwable e) {
        if (e == null) {
            return this;
        }

        final Utf8Sink sink = sink();
        final ObjHashSet<Throwable> dejaVu = tlSet.get();
        dejaVu.add(e);
        sink.putEOL();
        put0(sink, e);
        sink.putEOL();

        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0, n = trace.length; i < n; i++) {
            put(sink, trace[i]);
        }

        // Print suppressed exceptions, if any
        Throwable[] suppressed = e.getSuppressed();
        for (int i = 0, n = suppressed.length; i < n; i++) {
            put(sink, suppressed[i], trace, "Suppressed: ", "\t", dejaVu);
        }

        // Print cause, if any
        Throwable ourCause = e.getCause();
        if (ourCause != null) {
            put(sink, ourCause, trace, "Caused by: ", "", dejaVu);
        }

        return this;
    }

    @Override
    public void $() {
        sink().putEOL();
        Holder h = tl.get();
        h.seq.done(h.cursor);
    }

    @Override
    public LogRecord $hex(long value) {
        Numbers.appendHex(sink(), value, false);
        return this;
    }

    @Override
    public LogRecord $ts(long x) {
        sink().putISODate(x);
        return this;
    }

    public LogRecord advisory() {
        // Same as advisoryW()
        return addTimestamp(xAdvisoryW(), LogLevel.ADVISORY_HEADER);
    }

    public LogRecord advisoryW() {
        return addTimestamp(xAdvisoryW(), LogLevel.ADVISORY_HEADER);
    }

    public LogRecord critical() {
        // same as criticalW()
        return addTimestamp(xCriticalW(), LogLevel.CRITICAL_HEADER);
    }

    public LogRecord debug() {
        return addTimestamp(xdebug(), LogLevel.DEBUG_HEADER);
    }

    public LogRecord debugW() {
        return addTimestamp(xDebugW(), LogLevel.DEBUG_HEADER);
    }

    public LogRecord error() {
        return addTimestamp(xerror(), LogLevel.ERROR_HEADER);
    }

    public LogRecord errorW() {
        return addTimestamp(xErrorW(), LogLevel.ERROR_HEADER);
    }

    public Seq getCriticalSequence() {
        return criticalSeq;
    }

    public LogRecord info() {
        return addTimestamp(xinfo(), LogLevel.INFO_HEADER);
    }

    public LogRecord infoW() {
        return addTimestamp(xInfoW(), LogLevel.INFO_HEADER);
    }

    @Override
    public LogRecord put(char c) {
        sink().put(c);
        return this;
    }

    @Override
    public LogRecord put(byte b) {
        sink().put(b);
        return this;
    }

    @Override
    public Utf8Sink put(@Nullable NativeChunk us) {
        sink().put(us);
        return this;
    }

    @Override
    public Utf8Sink putNonAscii(long lo, long hi) {
        sink().putNonAscii(lo, hi);
        return this;
    }

    @Override
    public LogRecord ts() {
        sink().putISODate(Files.currentTimeMicros());
        return this;
    }

    @Override
    public LogRecord utf8(@Nullable CharSequence sequence) {
        if (sequence == null) {
            sink().putAscii("null");
        } else {
            sink().put(sequence);
        }
        return this;
    }

    @Override
    public LogRecord $utf8(long lo, long hi) {
        sink().putNonAscii(lo, hi);
        return this;
    }

    @Override
    public LogRecord xDebugW() {
        return nextWaiting(debugSeq, debugRing, LogLevel.DEBUG);
    }

    public LogRecord xErrorW() {
        return nextWaiting(errorSeq, errorRing, LogLevel.ERROR);
    }

    /**
     * Guaranteed log delivery at INFO level. The calling thread will wait for async logger
     * to become available instead of discarding log message.
     *
     * @return log record API
     */
    public LogRecord xInfoW() {
        return nextWaiting(infoSeq, infoRing, LogLevel.INFO);
    }

    private static void put(Utf8Sink sink, StackTraceElement e) {
        sink.putAscii("\tat ");
        sink.putAscii(e.getClassName());
        sink.putAscii('.');
        sink.putAscii(e.getMethodName());
        if (e.isNativeMethod()) {
            sink.putAscii("(Native Method)");
        } else {
            if (e.getFileName() != null && e.getLineNumber() > -1) {
                sink.putAscii('(').put(e.getFileName()).putAscii(':').put(e.getLineNumber()).putAscii(')');
            } else if (e.getFileName() != null) {
                sink.putAscii('(').put(e.getFileName()).putAscii(')');
            } else {
                sink.putAscii("(Unknown Source)");
            }
        }
        sink.put(Files.EOL);
    }

    private static void put(
            Utf8Sink sink,
            Throwable throwable,
            StackTraceElement[] enclosingTrace,
            String caption,
            String prefix,
            Set<Throwable> dejaVu
    ) {
        if (dejaVu.contains(throwable)) {
            sink.putAscii("\t[CIRCULAR REFERENCE:");
            put0(sink, throwable);
            sink.putAscii(']');
        } else {
            dejaVu.add(throwable);

            // Compute number of frames in common between this and enclosing trace
            StackTraceElement[] trace = throwable.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
                m--;
                n--;
            }
            int framesInCommon = trace.length - 1 - m;

            sink.put(prefix).put(caption);
            put0(sink, throwable);
            sink.putEOL();

            for (int i = 0; i <= m; i++) {
                sink.put(prefix);
                put(sink, trace[i]);
            }
            if (framesInCommon != 0) {
                sink.put(prefix).putAscii("\t...").put(framesInCommon).putAscii(" more");
            }

            // Print suppressed exceptions, if any
            Throwable[] suppressed = throwable.getSuppressed();
            for (int i = 0, k = suppressed.length; i < k; i++) {
                put(sink, suppressed[i], trace, "Suppressed: ", prefix + '\t', dejaVu);
            }

            // Print cause, if any
            Throwable cause = throwable.getCause();
            if (cause != null) {
                put(sink, cause, trace, "Caused by: ", prefix, dejaVu);
            }
        }
    }

    private static void put0(Utf8Sink sink, Throwable e) {
        sink.putAscii(e.getClass().getName());
        if (e.getMessage() != null) {
            sink.putAscii(": ").put(e.getMessage());
        }
    }

    protected LogRecord addTimestamp(LogRecord rec, String level) {
        return rec.ts().$(level).$(name);
    }

    protected LogRecord nextWaiting(Seq seq, RingQueue<LogRecordUtf8Sink> ring, int level) {
        if (seq == null) {
            return NullLogRecord.INSTANCE;
        }
        return prepareLogRecord(seq, ring, level, seq.nextBully());
    }

    @NotNull
    protected LogRecord prepareLogRecord(Seq seq, RingQueue<LogRecordUtf8Sink> ring, int level, long cursor) {
        Holder h = tl.get();
        h.cursor = cursor;
        h.seq = seq;
        h.ring = ring;
        LogRecordUtf8Sink r = ring.get(cursor);
        r.setLevel(level);
        r.clear();
        return this;
    }

    protected LogRecordUtf8Sink sink() {
        Holder h = tl.get();
        return h.ring.get(h.cursor);
    }

    protected LogRecord xAdvisoryW() {
        return nextWaiting(advisorySeq, advisoryRing, LogLevel.ADVISORY);
    }

    protected LogRecord xCriticalW() {
        return nextWaiting(criticalSeq, criticalRing, LogLevel.CRITICAL);
    }

    protected static class Holder {
        protected long cursor;
        protected RingQueue<LogRecordUtf8Sink> ring;
        protected Seq seq;
    }

    protected static class ThreadLocalCursor extends ThreadLocal<Holder> {
        @Override
        protected Holder initialValue() {
            return new Holder();
        }
    }
}
