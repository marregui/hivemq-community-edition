package com.hivemq.tk;

public interface LogWriter extends Job {
    void bindProperties(LogFactory factory);
}
