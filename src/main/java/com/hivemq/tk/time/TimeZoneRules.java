package com.hivemq.tk.time;

public interface TimeZoneRules {
    long getNextDST(long utcEpoch, int year, boolean leap);

    long getOffset(long utcEpoch, int year, boolean leap);

    long getOffset(long utcEpoch);
}
