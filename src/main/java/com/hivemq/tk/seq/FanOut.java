package com.hivemq.tk.seq;

import com.hivemq.tk.Unsafe;
import com.hivemq.tk.ds.ObjList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FanOut implements Barrier {
    private static final long HOLDER = Unsafe.fieldOffset(FanOut.class, "holder");
    private final @NotNull FanOutHolder holder;
    private @Nullable Barrier barrier;

    public FanOut(final @NotNull Barrier... barriers) {
        final FanOutHolder h = new FanOutHolder();
        for (int i = 0; i < barriers.length; i++) {
            final Barrier sq = barriers[i];
            h.barriers.add(sq);
            if (sq.getWaitStrategy().acceptSignal()) {
                h.waitStrategies.add(sq.getWaitStrategy());
            }
        }
        h.setupWaitStrategy();
        holder = h;
    }

    public static @NotNull FanOut to(final @NotNull Barrier barrier) {
        return new FanOut().and(barrier);
    }

    public @NotNull FanOut and(final @NotNull Barrier barrier) {
        FanOutHolder _new;
        Barrier root = null;

        final long current = this.barrier != null ? this.barrier.current() : -1;
        Unsafe.UNSAFE.loadFence();

        do {
            final FanOutHolder h = this.holder;
            // read barrier to make sure "holder" read doesn't fall below this

            if (h.barriers.indexOf(barrier) > -1) {
                return this;
            }

            if (root == null && this.barrier != null) {
                root = barrier.root();
                root.setBarrier(this.barrier);
                root.setCurrent(current);
                Unsafe.UNSAFE.storeFence();
            }
            _new = new FanOutHolder();
            _new.barriers.addAll(h.barriers);
            _new.barriers.add(barrier);
            _new.waitStrategies.addAll(h.waitStrategies);

            if (barrier.getWaitStrategy().acceptSignal()) {
                _new.waitStrategies.add(barrier.getWaitStrategy());
            }
            _new.setupWaitStrategy();
            if (Unsafe.UNSAFE.compareAndSwapObject(this, HOLDER, h, _new)) {
                // catch up with others
                if (root != null && this.barrier != null) {
                    root.setCurrent(this.barrier.current());
                }
                break;
            }
        } while (true);

        Unsafe.UNSAFE.storeFence();
        return this;
    }

    // this is firebug bug, the code does not write to array elements
    // it has to take a copy of this.barriers as this reference can change while
    // loop is in flight
    @Override
    public long availableIndex(final long lo) {
        long l = Objects.requireNonNull(barrier).availableIndex(lo);
        final ObjList<Barrier> barriers = holder.barriers;
        for (int i = 0, n = barriers.size(); i < n; i++) {
            l = Math.min(l, barriers.getQuick(i).availableIndex(lo));
        }
        return l;
    }

    @Override
    public long current() {
        return Objects.requireNonNull(barrier).current();
    }

    @Override
    public @NotNull WaitStrategy getWaitStrategy() {
        return holder.waitStrategy;
    }

    public void remove(final @NotNull SingleConsumerSeq barrier) {
        Unsafe.UNSAFE.storeFence();
        FanOutHolder _new;
        do {
            final FanOutHolder h = this.holder;
            // read barrier to make sure "holder" read doesn't fall below this

            if (h.barriers.indexOf(barrier) == -1) {
                break;
            }
            _new = new FanOutHolder();
            for (int i = 0, n = h.barriers.size(); i < n; i++) {
                final Barrier sq = h.barriers.getQuick(i);
                if (sq != barrier) {
                    _new.barriers.add(sq);
                }
            }
            final WaitStrategy that = barrier.getWaitStrategy();
            if (that.acceptSignal()) {
                for (int i = 0, n = h.waitStrategies.size(); i < n; i++) {
                    final WaitStrategy ws = h.waitStrategies.getQuick(i);
                    if (ws != that) {
                        _new.waitStrategies.add(ws);
                    }
                }
            } else {
                _new.waitStrategies.addAll(h.waitStrategies);
            }
            _new.setupWaitStrategy();
            if (Unsafe.UNSAFE.compareAndSwapObject(this, HOLDER, h, _new)) {
                break;
            }
        } while (true);
        barrier.reset();
    }

    @Override
    public @NotNull Barrier root() {
        return barrier != null ? barrier.root() : this;
    }

    public void setBarrier(final @NotNull Barrier barrier) {
        this.barrier = barrier;
        final ObjList<Barrier> barriers = holder.barriers;
        for (int i = 0, n = barriers.size(); i < n; i++) {
            barriers.getQuick(i).root().setBarrier(barrier);
        }
    }

    @Override
    public void setCurrent(final long value) {
        final ObjList<Barrier> barriers = holder.barriers;
        for (int i = 0, n = barriers.size(); i < n; i++) {
            barriers.getQuick(i).setCurrent(value);
        }
    }

    @Override
    public @NotNull Barrier then(final @NotNull Barrier barrier) {
        barrier.setBarrier(this);
        return barrier;
    }

}
