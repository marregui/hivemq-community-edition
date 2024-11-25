package com.hivemq.tk.seq;


import com.hivemq.tk.Numbers;
import com.hivemq.tk.Unsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

abstract class AbstractMultiSeq extends AbstractSingleSeq {
    private final int @NotNull [] flags;
    private final int mask;
    private final int shift;

    AbstractMultiSeq(final int cycle, final @Nullable WaitStrategy waitStrategy) {
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
        for (final long hi = this.value + 1; l < hi && available0(l); l++) ;
        return l - 1;
    }

    @Override
    public long current() {
        return value;
    }

    @Override
    public void done(final long cursor) {
        Unsafe.UNSAFE.putOrderedInt(flags,
                ((cursor & mask) << Unsafe.INT_SCALE) + Unsafe.INT_OFFSET,
                (int) (cursor >>> shift));
        barrier.getWaitStrategy().signal();
    }

    @Override
    public void setCurrent(final long value) {
        this.value = value;
    }

    private boolean available0(final long lo) {
        final long off = (((lo & mask)) << Unsafe.INT_SCALE) + Unsafe.INT_OFFSET;
        return Unsafe.UNSAFE.getIntVolatile(flags, off) == (int) (lo >>> shift);
    }
}
