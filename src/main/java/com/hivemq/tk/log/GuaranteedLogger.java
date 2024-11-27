package com.hivemq.tk.log;

import com.hivemq.tk.seq.RingQueue;
import com.hivemq.tk.seq.Seq;

/**
 * Same as #Logger but does not lose messages.
 */
public final class GuaranteedLogger extends AbstractLogRecord {

    GuaranteedLogger(
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
