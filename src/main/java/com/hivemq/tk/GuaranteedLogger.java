package com.hivemq.tk;

import com.hivemq.tk.time.MicrosClock;

/**
 * Same as #Logger but does not lose messages.
 */
public final class GuaranteedLogger extends AbstractLogRecord {

    GuaranteedLogger(
            MicrosClock clock,
            CharSequence name,
            RingQueue<LogRecordUtf8Sink> debugRing,
            Sequence debugSeq,
            RingQueue<LogRecordUtf8Sink> infoRing,
            Sequence infoSeq,
            RingQueue<LogRecordUtf8Sink> errorRing,
            Sequence errorSeq,
            RingQueue<LogRecordUtf8Sink> criticalRing,
            Sequence criticalSeq,
            RingQueue<LogRecordUtf8Sink> advisoryRing,
            Sequence advisorySeq
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
        return xAdvisoryW();
    }

    @Override
    public LogRecord xcritical() {
        return xCriticalW();
    }

    @Override
    public LogRecord xdebug() {
        return xDebugW();
    }

    @Override
    public LogRecord xerror() {
        return xErrorW();
    }

    @Override
    public LogRecord xinfo() {
        return xInfoW();
    }
}
