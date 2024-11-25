package com.hivemq.tk.seq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractSingleSeq extends AbstractSeq implements Seq {

    AbstractSingleSeq(final @Nullable WaitStrategy waitStrategy) {
        super(waitStrategy);
    }

    AbstractSingleSeq() {
        this(NullWaitStrategy.INSTANCE);
    }

    @Override
    public long nextBully() {
        long cursor;
        while ((cursor = next()) < 0) {
            bully();
        }
        return cursor;
    }

    @Override
    public @NotNull Barrier root() {
        return barrier != OpenBarrier.INSTANCE ? barrier.root() : this;
    }

    @Override
    public void setBarrier(final @NotNull Barrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public @NotNull Barrier then(final @NotNull Barrier barrier) {
        barrier.setBarrier(this);
        return barrier;
    }

    @Override
    public long waitForNext() {
        long r;
        WaitStrategy waitStrategy = getWaitStrategy();
        while ((r = next()) < 0) {
            if (r == -2) {
                continue;
            }
            waitStrategy.await();
        }
        return r;
    }

    private void bully() {
        barrier.getWaitStrategy().signal();
    }
}
