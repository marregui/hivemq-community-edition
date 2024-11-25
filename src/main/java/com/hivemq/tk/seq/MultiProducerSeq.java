package com.hivemq.tk.seq;

import org.jetbrains.annotations.Nullable;

public class MultiProducerSeq extends AbstractMultiSeq {
    private final int cycle;

    public MultiProducerSeq(final int cycle) {
        this(cycle, NullWaitStrategy.INSTANCE);
    }

    private MultiProducerSeq(final int cycle, final @Nullable WaitStrategy waitStrategy) {
        super(cycle, waitStrategy);
        this.cycle = cycle;
    }

    @Override
    public long next() {
        // reading cache before value is essential
        // because algo relies on barrier inserted
        // by value read.
        final long cached = cache;
        final long current = value;
        final long next = current + 1;
        final long lo = next - cycle;
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
