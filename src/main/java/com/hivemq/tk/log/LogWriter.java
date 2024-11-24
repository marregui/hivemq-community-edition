package com.hivemq.tk.log;

import com.hivemq.tk.Job;

public interface LogWriter extends Job {
    void bindProperties(LogFactory factory);
}
