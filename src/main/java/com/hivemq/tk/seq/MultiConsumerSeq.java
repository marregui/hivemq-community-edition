

package com.hivemq.tk.seq;

public class MultiConsumerSeq extends AbstractMultiSeq {

    public MultiConsumerSeq(int cycle) {
        this(cycle, null);
    }

    public MultiConsumerSeq(int cycle, WaitStrategy waitStrategy) {
        super(cycle, waitStrategy);
    }

    public <T> void consumeAll(RingQueue<T> queue, QueueConsumer<T> consumer) {
        long cursor;
        do {
            cursor = next();
            if (cursor > -1) {
                consumer.consume(queue.get(cursor));
                done(cursor);
            } else if (cursor == -2) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException ignore) {
                }
            }
        } while (cursor != -1);
    }

    @Override
    public long next() {
        long cached = cache;
        long current = value;
        long next = current + 1;

        if (next > cached) {
            long avail = barrier.availableIndex(next);
            if (avail > cached) {
                setCacheFenced(avail);
                if (next > avail) {
                    return -1;
                }
            } else {
                return -1;
            }
        }
        return casValue(current, next) ? next : -2;
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
}
