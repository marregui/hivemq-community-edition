package com.hivemq.tk;

public interface Sequence extends Barrier {

    void done(long cursor);

    /***
     * Returns next position to publish to / fetch from
     * @return Queue index to use. Returns -1 if full / empty or -2 if race is lost
     */
    long next();

    long nextBully();
}
