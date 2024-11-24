package com.hivemq.tk;

import com.hivemq.tk.time.MicrosClock;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Worker extends Thread {
    public static final MicrosClock CLOCK_MICROS = MicrosClock.INSTANCE;
    private final String criticalErrorLine;
    private final SOCountDownLatch haltLatch;
    private final boolean haltOnError;
    private final AtomicLong jobStartMicros = new AtomicLong();
    private final ObjHashSet<? extends Job> jobs;
    private final AtomicReference<Lifecycle> lifecycle = new AtomicReference<>(Lifecycle.BORN);
    private final Log log;
    private final long napThreshold;
    private final OnHaltAction onHaltAction;
    private final String poolName;
    private final Job.RunStatus runStatus = () -> lifecycle.get() == Lifecycle.HALTED;
    private final long sleepMs;
    private final long sleepThreshold;
    private final int workerId;
    private final long yieldThreshold;

    public Worker(
            String poolName,
            int workerId, ObjHashSet<? extends Job> jobs,
            SOCountDownLatch haltLatch,
            @Nullable OnHaltAction onHaltAction,
            boolean haltOnError,
            long yieldThreshold,
            long napThreshold,
            long sleepThreshold,
            long sleepMs,
            @Nullable Log log) {
        assert yieldThreshold > 0L;
        this.setName(poolName + '_' + workerId);
        this.poolName = poolName;
        this.workerId = workerId;
        this.jobs = jobs;
        this.haltLatch = haltLatch;
        this.onHaltAction = onHaltAction;
        this.haltOnError = haltOnError;
        this.criticalErrorLine = "0000-00-00T00:00:00.000000Z C Unhandled exception in worker " + getName();
        this.yieldThreshold = yieldThreshold;
        this.napThreshold = napThreshold;
        this.sleepThreshold = sleepThreshold;
        this.sleepMs = sleepMs;
        this.log = log;
    }

    public void halt() {
        lifecycle.set(Lifecycle.HALTED);
    }

    @Override
    public void run() {
        Throwable ex = null;
        try {
            if (lifecycle.compareAndSet(Lifecycle.BORN, Lifecycle.RUNNING)) {

                String workerName = getName();
                if (log != null) {
                    log.info().$("os scheduled worker started [name=").$(workerName).I$();
                }

                // setup eager jobs
                for (int i = 0, n = jobs.size(); i < n; i++) {
                    Unsafe.UNSAFE.loadFence();
                    try {
                        Job job = jobs.get(i);
                        if (job instanceof EagerThreadSetup) {
                            ((EagerThreadSetup) job).setup();
                        }
                    } finally {
                        Unsafe.UNSAFE.storeFence();
                    }
                }

                // enter main loop
                long ticker = 0L;
                while (lifecycle.get() == Lifecycle.RUNNING) {
                    boolean runAsap = false;
                    // measure latency of all jobs tick
                    jobStartMicros.lazySet(CLOCK_MICROS.getTicks());
                    for (int i = 0, n = jobs.size(); i < n; i++) {
                        Unsafe.UNSAFE.loadFence();
                        try {
                            runAsap |= jobs.get(i).run(workerId, runStatus);
                        } catch (Throwable e) {
                            if (log != null) {
                                log.critical()
                                        .$("unhandled error [job=")
                                        .$(jobs.get(i).toString())
                                        .$(", ex=")
                                        .$(e)
                                        .I$();
                            } else {
                                stdErrCritical(e); // log regardless
                            }
                            if (haltOnError) {
                                throw e;
                            }
                        } finally {
                            Unsafe.UNSAFE.storeFence();
                        }
                    }

                    if (runAsap) {
                        ticker = 0;
                        continue;
                    }
                    if (++ticker < 0) {
                        ticker = sleepThreshold + 1; // overflow
                    }
                    if (ticker > sleepThreshold) {
                        sleep(sleepMs);
                    } else if (ticker > napThreshold) {
                        sleep(1);
                    } else if (ticker > yieldThreshold) {
                        try {
                            Thread.sleep(0);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
            }
        } catch (Throwable e) {
            ex = e;
            stdErrCritical(e);
        } finally {
            if (onHaltAction != null) {
                try {
                    onHaltAction.run(ex);
                    if (log != null) {
                        log.info().$("cleaned worker [name=").$(poolName).$(", worker=").$(workerId).I$();
                    }
                } catch (Throwable t) {
                    stdErrCritical(t);
                }
            }
            haltLatch.countDown();
            if (log != null) {
                log.info().$("os scheduled worker stopped [name=").$(getName()).I$();
            }
        }
    }

    public static void sleep(long millis) {
        long t = System.currentTimeMillis();
        long deadline = millis;
        while (deadline > 0) {
            try {
                Thread.sleep(deadline);
                break;
            } catch (InterruptedException e) {
                long t2 = System.currentTimeMillis();
                deadline -= t2 - t;
                t = t2;
            }
        }
    }

    private void stdErrCritical(Throwable e) {
        System.err.println(criticalErrorLine);
        e.printStackTrace(System.err);
    }

    private enum Lifecycle {
        BORN,
        RUNNING,
        HALTED
    }

    @FunctionalInterface
    public interface OnHaltAction {
        void run(Throwable ex);
    }
}
