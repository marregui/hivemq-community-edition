package com.hivemq.tk.seq;

public interface Seq extends Barrier {

    void done(final long cursor);

    /***
     * Returns next position to publish to/fetch from
     * @return Queue index to use. Returns -1 if full/empty or -2 if race is lost
     */
    long next();

    long nextBully();
}
