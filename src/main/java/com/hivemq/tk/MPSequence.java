package com.hivemq.tk;

/**
 * M - multi thread
 * P - producer
 */
public class MPSequence extends AbstractMSequence {
    private final int cycle;

    public MPSequence(int cycle) {
        this(cycle, NullWaitStrategy.INSTANCE);
    }

    private MPSequence(int cycle, WaitStrategy waitStrategy) {
        super(cycle, waitStrategy);
        this.cycle = cycle;
    }

    @Override
    public long next() {
        // reading cache before value is essential because algo relies on barrier inserted by value read.
        long cached = cache;
        long current = value;
        long next = current + 1;
        long lo = next - cycle;

        if (lo <= cached) {
            return casValue(current, next) ? next : -2;
        }

        final long avail = barrier.availableIndex(lo);

        if (avail > cached) {
            setCacheFenced(avail);
            if (lo <= avail) {
                return casValue(current, next) ? next : -2;
            }
        }
        return -1;
    }
}
