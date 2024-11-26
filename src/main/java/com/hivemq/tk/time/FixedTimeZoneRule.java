package com.hivemq.tk.time;

public class FixedTimeZoneRule implements TimeZoneRules {
    private final long offset;

    public FixedTimeZoneRule(long offset) {
        this.offset = offset;
    }

    @Override
    public long getNextDST(long utcEpoch, int year, boolean leap) {
        return Long.MAX_VALUE;
    }

    @Override
    public long getNextDST(long utcEpoch) {
        return Long.MAX_VALUE;
    }

    @Override
    public long getOffset(long utcEpoch, int year, boolean leap) {
        return offset;
    }

    @Override
    public long getOffset(long utcEpoch) {
        return offset;
    }
}
