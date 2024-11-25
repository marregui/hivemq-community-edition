package com.hivemq.tk.seq;

import com.hivemq.tk.QueueConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleConsumerSeq extends AbstractSingleSeq {

    public SingleConsumerSeq() {
    }

    public SingleConsumerSeq(final @Nullable WaitStrategy waitStrategy) {
        super(waitStrategy);
    }

    public SingleConsumerSeq(long value, WaitStrategy waitStrategy) {
        super(waitStrategy);
        setCurrent(value);
    }

    public long available() {
        return cache + 1;
    }

    @Override
    public long availableIndex(final long lo) {
        return this.value;
    }

    public <T> boolean consumeAll(final @NotNull RingQueue<T> queue, final @NotNull QueueConsumer<T> consumer) {
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
    public void done(final long cursor) {
        this.value = cursor;
        barrier.getWaitStrategy().signal();
    }

    @Override
    public long next() {
        final long next = getValue();
        if (next < cache) {
            return next + 1;
        }
        return next0(next + 1);
    }

    // The method is final is because we call it from
    // the constructor.
    @Override
    public final void setCurrent(final long value) {
        this.value = value;
    }

    private long next0(final long next) {
        cache = barrier.availableIndex(next);
        return next > cache ? -1 : next;
    }

    public void clear() {
        while (true) {
            final long n = next();
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
