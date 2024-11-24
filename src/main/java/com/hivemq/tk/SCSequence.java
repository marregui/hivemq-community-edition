package com.hivemq.tk;

//single consumer sequence 
public class SCSequence extends AbstractSSequence {

    public SCSequence() {
    }

    public long available() {
        return cache + 1;
    }

    @Override
    public long availableIndex(long lo) {
        return this.value;
    }

    public <T> boolean consumeAll(RingQueue<T> queue, QueueConsumer<T> consumer) {
        long cursor = next();
        if (cursor < 0) {
            return false;
        }

        do {
            if (cursor > -1) {
                final long available = available();
                while (cursor < available) {
                    consumer.consume(queue.get(cursor++));
                }
                done(available - 1);
            }
        } while ((cursor = next()) != -1);

        return true;
    }

    @Override
    public long current() {
        return value;
    }

    @Override
    public void done(long cursor) {
        this.value = cursor;
        barrier.getWaitStrategy().signal();
    }

    @Override
    public long next() {
        long next = getValue();
        if (next < cache) {
            return next + 1;
        }

        return next0(next + 1);
    }

    // The method is final is because we call it from
    // the constructor.
    @Override
    public final void setCurrent(long value) {
        this.value = value;
    }

    private long next0(long next) {
        cache = barrier.availableIndex(next);
        return next > cache ? -1 : next;
    }

    public void clear() {
        while (true) {
            long n = next();
            if (n == -1) {
                break;
            }
            if (n != -2) {
                done(n);
            }
        }
    }

    public void reset() {
        cache = -1;
        value = -1;
        barrier = OpenBarrier.INSTANCE;
    }
}
