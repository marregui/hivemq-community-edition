package com.hivemq.tk;

import com.hivemq.tk.ds.ObjHashSet;
import com.hivemq.tk.ds.ObjList;
import com.hivemq.tk.ds.SOCountDownLatch;
import com.hivemq.tk.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerPool implements Closeable {
    private final @NotNull AtomicBoolean closed = new AtomicBoolean();
    private final @NotNull ObjList<Closeable> freeOnExit = new ObjList<>();
    private final @NotNull SOCountDownLatch halted;
    private final @NotNull String poolName;
    private final int priority;
    private final @NotNull AtomicBoolean running = new AtomicBoolean();
    private final long sleepMs;
    private final long sleepThreshold;
    private final @NotNull SOCountDownLatch started = new SOCountDownLatch(1);
    private final @NotNull ObjList<ObjList<Closeable>> threadLocalCleaners;
    private final int workerCount;
    private final @NotNull ObjList<ObjHashSet<Job>> workerJobs;
    private final @NotNull ObjList<Worker> workers = new ObjList<>();

    public WorkerPool(final @NotNull String poolName, final int workerCount) {
        this.workerCount = workerCount;
        this.halted = new SOCountDownLatch(workerCount);
        this.poolName = poolName;
        this.sleepThreshold = 10000;
        this.sleepMs = 10;
        this.priority = Thread.NORM_PRIORITY;
        this.workerJobs = new ObjList<>(workerCount);
        this.threadLocalCleaners = new ObjList<>(workerCount);
        for (int i = 0; i < workerCount; i++) {
            workerJobs.add(new ObjHashSet<>());
            threadLocalCleaners.add(new ObjList<>());
        }
    }

    private static <T extends Closeable> void freeObjListAndClear(final @Nullable ObjList<T> list) {
        if (list != null) {
            for (int i = 0, n = list.size(); i < n; i++) {
                Misc.free(list.getQuick(i));
            }
            list.clear();
        }
    }

    public void assign(final @NotNull Job job) {
        assert !running.get() && !closed.get();
        for (int i = 0; i < workerCount; i++) {
            workerJobs.getQuick(i).add(job);
        }
    }

    @Override
    public void close() {
        halt();
    }

    public void halt() {
        if (closed.compareAndSet(false, true)) {
            if (running.compareAndSet(true, false)) {
                started.await();
                for (int i = 0; i < workerCount; i++) {
                    workers.getQuick(i).halt();
                }
                halted.await();
            }
            workers.clear(); // Worker is not closable
            freeObjListAndClear(freeOnExit);
        }
    }

    @TestOnly
    public void pause() {
        if (running.compareAndSet(true, false)) {
            started.await();
            for (int i = 0; i < workerCount; i++) {
                workers.getQuick(i).halt();
            }
            halted.await();
        }
        workers.clear();
    }

    public void start() {
        start(null);
    }

    public void start(final @Nullable Log log) {
        if (!closed.get() && running.compareAndSet(false, true)) {
            for (int i = 0; i < workerCount; i++) {
                final int index = i;
                final Worker worker = new Worker(poolName,
                        i,
                        workerJobs.getQuick(i),
                        halted,
                        ex -> freeObjListAndClear(threadLocalCleaners.getQuick(index)),
                        sleepThreshold,
                        sleepMs,
                        log);
                worker.setPriority(priority);
                worker.setDaemon(false);
                workers.add(worker);
                worker.start();
            }
            if (log != null) {
                log.info().$("worker pool started [pool=").$(poolName).I$();
            }
            started.countDown();
        }
    }
}
