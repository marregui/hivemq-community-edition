package com.hivemq.tk;

import com.hivemq.tk.ds.ObjHashSet;
import com.hivemq.tk.ds.ObjList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerPool implements Closeable {
    private final AtomicBoolean closed = new AtomicBoolean();
    private final boolean daemons = false;
    private final ObjList<Closeable> freeOnExit = new ObjList<>();
    private final boolean haltOnError = false;
    private final SOCountDownLatch halted;
    private final long napThreshold;
    private final String poolName;
    private final int priority;
    private final AtomicBoolean running = new AtomicBoolean();
    private final long sleepMs;
    private final long sleepThreshold;
    private final SOCountDownLatch started = new SOCountDownLatch(1);
    private final ObjList<ObjList<Closeable>> threadLocalCleaners;
    private final int[] workerAffinity;
    private final int workerCount;
    private final ObjList<ObjHashSet<Job>> workerJobs;
    private final ObjList<Worker> workers = new ObjList<>();
    private final long yieldThreshold;

    public WorkerPool(String poolName, int workerCount) {
        this.workerCount = workerCount;
        this.workerAffinity = Misc.getWorkerAffinity(workerCount);
        this.halted = new SOCountDownLatch(workerCount);
        this.poolName = poolName == null?"worker": poolName;
        this.yieldThreshold = 10;
        this.napThreshold = 7000;
        this.sleepThreshold = 10000;
        this.sleepMs = 10;
        this.priority = Thread.NORM_PRIORITY;

        assert this.workerAffinity.length == workerCount;

        this.workerJobs = new ObjList<>(workerCount);
        this.threadLocalCleaners = new ObjList<>(workerCount);
        for (int i = 0; i < workerCount; i++) {
            workerJobs.add(new ObjHashSet<>());
            threadLocalCleaners.add(new ObjList<>());
        }
    }

    /**
     * Assigns job instance to all workers. Job member variables
     * could be accessed by multiple threads at the same time. Jobs cannot
     * be added after pool is started.
     *
     * @param job instance of job
     */
    public void assign(Job job) {
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
            Misc.freeObjListAndClear(freeOnExit);
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

    public void start(@Nullable Log log) {
        if (!closed.get() && running.compareAndSet(false, true)) {
            for (int i = 0; i < workerCount; i++) {
                final int index = i;
                Worker worker = new Worker(poolName,
                        i,
                        workerJobs.getQuick(i),
                        halted,
                        ex -> Misc.freeObjListAndClear(threadLocalCleaners.getQuick(index)),
                        haltOnError,
                        yieldThreshold,
                        napThreshold,
                        sleepThreshold,
                        sleepMs,
                        log);
                worker.setPriority(priority);
                worker.setDaemon(daemons);
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
