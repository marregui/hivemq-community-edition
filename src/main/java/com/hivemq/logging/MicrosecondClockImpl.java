package com.hivemq.logging;

public class MicrosecondClockImpl implements MicrosecondClock {
    public static final MicrosecondClock INSTANCE = new MicrosecondClockImpl();

    @Override
    public long getTicks() {
        return Timestamps.currentTimeMicros();
    }
}


