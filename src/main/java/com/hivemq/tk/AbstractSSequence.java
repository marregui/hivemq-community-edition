package com.hivemq.tk;

//single consumer or producer sequence
abstract class AbstractSSequence extends AbstractSequence implements Sequence {

    AbstractSSequence(WaitStrategy waitStrategy) {
        super(waitStrategy);
    }

    AbstractSSequence() {
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
    public Barrier root() {
        return barrier != OpenBarrier.INSTANCE ? barrier.root() : this;
    }

    @Override
    public void setBarrier(Barrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public Barrier then(Barrier barrier) {
        barrier.setBarrier(this);
        return barrier;
    }

    private void bully() {
        barrier.getWaitStrategy().signal();
    }
}
