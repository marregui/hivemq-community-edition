package com.hivemq.tk;

import com.hivemq.tk.ds.ObjHashSet;
import com.hivemq.tk.ds.SOCountDownLatch;
import com.hivemq.tk.log.Log;
import com.hivemq.tk.time.Timestamps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Worker extends Thread {
    private final @NotNull String criticalErrorLine;
    private final @NotNull SOCountDownLatch haltLatch;
    private final @NotNull AtomicLong jobStartMicros = new AtomicLong();
    private final @NotNull ObjHashSet<? extends Job> jobs;
    private final @NotNull AtomicReference<Lifecycle> lifecycle = new AtomicReference<>(Lifecycle.BORN);
    private final @Nullable Log log;
    private final @Nullable OnHaltAction onHaltAction;
    private final @NotNull String poolName;
    private final @NotNull Job.RunStatus runStatus = () -> lifecycle.get() == Lifecycle.HALTED;
    private final long sleepMs;
    private final long sleepThreshold;
    private final int workerId;

    public Worker(
            final @NotNull String poolName,
            final int workerId,
            final @NotNull ObjHashSet<? extends Job> jobs,
            final @NotNull SOCountDownLatch haltLatch,
            final @Nullable OnHaltAction onHaltAction,
            final long sleepThreshold,
            final long sleepMs,
            final @Nullable Log log) {
        this.setName(poolName + '_' + workerId);
        this.poolName = poolName;
        this.workerId = workerId;
        this.jobs = jobs;
        this.haltLatch = haltLatch;
        this.onHaltAction = onHaltAction;
        this.criticalErrorLine = "0000-00-00T00:00:00.000000Z C Unhandled exception in worker " + getName();
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
                final String workerName = getName();
                if (log != null) {
                    log.info().$("os scheduled worker started [name=").$(workerName).I$();
                }
                // setup eager jobs
                for (int i = 0, n = jobs.size(); i < n; i++) {
                    Unsafe.UNSAFE.loadFence();
                    try {
                        final Job job = jobs.get(i);
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
                    jobStartMicros.lazySet(Timestamps.currentTimeMicros());
                    for (int i = 0, n = jobs.size(); i < n; i++) {
                        Unsafe.UNSAFE.loadFence();
                        try {
                            runAsap |= jobs.get(i).run(workerId, runStatus);
                        } catch (final @NotNull Throwable e) {
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
                    } else if (ticker > 10) {
                        sleep();
                    }
                }
            }
        } catch (final @NotNull Throwable e) {
            ex = e;
            stdErrCritical(e);
        } finally {
            if (onHaltAction != null) {
                try {
                    onHaltAction.run(Objects.requireNonNull(ex));
                    if (log != null) {
                        log.info().$("cleaned worker [name=").$(poolName).$(", worker=").$(workerId).I$();
                    }
                } catch (final @NotNull Throwable t) {
                    stdErrCritical(t);
                }
            }
            haltLatch.countDown();
            if (log != null) {
                log.info().$("os scheduled worker stopped [name=").$(getName()).I$();
            }
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(0);
        } catch (final @NotNull InterruptedException ignore) {
        }
    }

    public static void sleep(final long millis) {
        long t = System.currentTimeMillis();
        long deadline = millis;
        while (deadline > 0) {
            try {
                Thread.sleep(deadline);
                break;
            } catch (final @NotNull InterruptedException e) {
                final long t2 = System.currentTimeMillis();
                deadline -= t2 - t;
                t = t2;
            }
        }
    }

    private void stdErrCritical(final @NotNull Throwable e) {
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
        void run(final @NotNull Throwable ex);
    }
}
