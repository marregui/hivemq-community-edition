package com.hivemq.tk;

public interface Barrier {
    long availableIndex(long lo);

    long current();

    WaitStrategy getWaitStrategy();

    Barrier root();

    void setBarrier(Barrier barrier);

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
    void setCurrent(long value);

    Barrier then(Barrier barrier);
}
