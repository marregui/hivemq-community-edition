package com.hivemq.tk;


import java.util.Arrays;

//abstract multi producer or consumer sequence 
abstract class AbstractMSequence extends AbstractSSequence {
    private final int[] flags;
    private final int mask;
    private final int shift;

    AbstractMSequence(int cycle, WaitStrategy waitStrategy) {
        super(waitStrategy);
        this.flags = new int[cycle];
        Arrays.fill(flags, -1);
        this.mask = cycle - 1;
        this.shift = Numbers.msb(cycle);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public long availableIndex(final long lo) {
        long l = lo;
        for (long hi = this.value + 1; l < hi && available0(l); l++) ;
        return l - 1;
    }

    @Override
    public long current() {
        return value;
    }

    @Override
    public void done(long cursor) {
        Unsafe.UNSAFE.putOrderedInt(flags, ((cursor & mask) << Unsafe.INT_SCALE) + Unsafe.INT_OFFSET, (int) (cursor >>> shift));
        barrier.getWaitStrategy().signal();
    }

    @Override
    public void setCurrent(long value) {
        this.value = value;
    }

    private boolean available0(long lo) {
        return Unsafe.UNSAFE.getIntVolatile(flags, (((lo & mask)) << Unsafe.INT_SCALE) + Unsafe.INT_OFFSET) == (int) (lo >>> shift);
    }
}
