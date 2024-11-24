package com.hivemq.tk.ds;

import com.hivemq.tk.Unsafe;

import java.util.concurrent.locks.LockSupport;

public class SOCountDownLatch {
    private static final long VALUE_OFFSET;
    private volatile int count;
    private volatile Thread waiter;

    public SOCountDownLatch(int count) {
        this.count = count;
    }

    public void await() {
        this.waiter = Thread.currentThread();
        while (getCount() > 0) {
            LockSupport.parkNanos( 5 * 1_000_000_000L);
        }
    }

    public boolean await(long nanos) {
        this.waiter = Thread.currentThread();
        if (getCount() == 0) {
            return true;
        }

        while (true) {
            long start = System.nanoTime();
            LockSupport.parkNanos(nanos);
            long elapsed = System.nanoTime() - start;

            if (elapsed < nanos) {
                if (getCount() == 0) {
                    return true;
                } else {
                    nanos -= elapsed;
                }
            } else {
                return getCount() == 0;
            }
        }
    }

    public void countDown() {
        do {
            int current = getCount();

            if (current < 1) {
                break;
            }

            int next = current - 1;
            if (Unsafe.cas(this, VALUE_OFFSET, current, next)) {
                if (next == 0) {
                    unparkWaiter();
                }
                break;
            }
        } while (true);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private void unparkWaiter() {
        Thread waiter = this.waiter;
        if (waiter != null) {
            LockSupport.unpark(waiter);
        }
    }

    static {
        VALUE_OFFSET = Unsafe.fieldOffset(SOCountDownLatch.class, "count");
    }
}
