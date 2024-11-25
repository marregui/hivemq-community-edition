package com.hivemq.tk.seq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Barrier {
    long availableIndex(final long lo);

    long current();

    @NotNull WaitStrategy getWaitStrategy();

    @NotNull Barrier root();

    void setBarrier(final @NotNull Barrier barrier);

    /**
     * When barrier is added mid-flight, it should assume the current
     * sequence of the publisher (otherwise known as barrier's barrier)
     * as its own. Such behaviour should prevent the newly joined
     * barrier from processing sequences since before the join time.
     * <p>
     * Most notably this is called by FanOut when new consumer joins
     * the cohort of existing consumers.
     *
     * @param value typically the sequence of the published
     */
    void setCurrent(final long value);

    @Nullable Barrier then(final @NotNull Barrier barrier);
}
