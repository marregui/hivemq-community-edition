

package com.hivemq.tk.seq;

public class SingleProducerSeq extends AbstractSingleSeq {
    private final int cycle;

    private SingleProducerSeq(int cycle, WaitStrategy waitStrategy) {
        super(waitStrategy);
        this.cycle = cycle;
    }

    public SingleProducerSeq(int cycle) {
        this(cycle, NullWaitStrategy.INSTANCE);
    }

    public long available() {
        return cache + cycle + 1;
    }

    @Override
    public long availableIndex(long lo) {
        return value;
    }

    @Override
    public long current() {
        return value;
    }

    @Override
    public void done(long cursor) {
        value = cursor;
        barrier.getWaitStrategy().signal();
    }

    @Override
    public long next() {
        long next = getValue() + 1;
        long lo = next - cycle;
        return lo > cache && lo > (cache = barrier.availableIndex(lo)) ? -1 : next;
    }

    @Override
    public void setCurrent(long value) {
        this.value = value;
    }
}
