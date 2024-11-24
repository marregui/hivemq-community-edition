package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public abstract class SynchronizedJob implements Job {
    private static final long LOCKED_OFFSET = Unsafe.fieldOffset(SynchronizedJob.class, "locked");

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private volatile int locked = 0;

    @Override
    public boolean run(int workerId, @NotNull RunStatus runStatus) {
        if (Unsafe.UNSAFE.compareAndSwapInt(this, LOCKED_OFFSET, 0, 1)) {
            try {
                return runSerially();
            } finally {
                locked = 0;
            }
        }
        return false;
    }

    public boolean run(int workerId) {
        return run(workerId, Job.RUNNING_STATUS);
    }

    protected abstract boolean runSerially();
}
