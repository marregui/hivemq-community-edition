package com.hivemq.tk.log;

import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.Seq;
import com.hivemq.tk.time.MicrosClock;

/**
 * Builds and sends log messages to writer thread. Log messages are constructed using "builder" pattern,
 * which usually begins with "level method" {@link #debug()}, {@link #info()} or {@link #error()} followed by
 * $(x) to append log message content and must terminate with {@link #$()}. There are $(x) methods for all types
 * of "x" parameter to help avoid string concatenation and string creation in general.
 * <p>
 * <code>
 * private static final Log LOG = LogFactory.getLog(MyClass.class);
 * ...
 * LOG.info().$("Hello world: ").$(123).$();
 * </code>
 * <p>
 * Logger appends messages to native memory buffer and dispatches buffer to writer thread queue with {@link #$()} call.
 * When writer queue is full all logger method calls between level and $() become no-ops. In this case queue size
 * have to be increased or choice of log storage has to be reviewed. Depending on complexity of log message
 * structure it should be possible to log between 1,000,000 and 10,000,000 messages per second to SSD device.
 * </p>
 */
public final class Logger extends AbstractLogRecord implements Log {
    Logger(
            MicrosClock clock,
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
        super(
                clock,
                name,
                debugRing,
                debugSeq,
                infoRing,
                infoSeq,
                errorRing,
                errorSeq,
                criticalRing,
                criticalSeq,
                advisoryRing,
                advisorySeq
        );
    }

    @Override
    public LogRecord xadvisory() {
        return next(advisorySeq, advisoryRing, LogLevel.ADVISORY);
    }

    @Override
    public LogRecord xcritical() {
        return next(criticalSeq, criticalRing, LogLevel.CRITICAL);
    }

    @Override
    public LogRecord xdebug() {
        return next(debugSeq, debugRing, LogLevel.DEBUG);
    }

    @Override
    public LogRecord xerror() {
        return next(errorSeq, errorRing, LogLevel.ERROR);
    }

    @Override
    public LogRecord xinfo() {
        return next(infoSeq, infoRing, LogLevel.INFO);
    }

    private LogRecord next(Seq seq, RingQueue<LogRecordUtf8Sink> ring, int level) {
        if (seq == null) {
            return NullLogRecord.INSTANCE;
        }

        final long cursor = seq.next();
        if (cursor < 0) {
            return NullLogRecord.INSTANCE;
        }
        return prepareLogRecord(seq, ring, level, cursor);
    }
}
