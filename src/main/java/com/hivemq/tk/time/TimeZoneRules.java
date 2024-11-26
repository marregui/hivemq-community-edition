package com.hivemq.tk.time;

public interface TimeZoneRules {
    long getNextDST(long utcEpoch, int year, boolean leap);

    /**
     * Computes UTC time for the next Daylight Saving Transition
     *
     * @param utcEpoch arbitrary point in time, UTC epoch time
     * @return UTC epoch
     */
    long getNextDST(long utcEpoch);

    long getOffset(long utcEpoch, int year, boolean leap);

    long getOffset(long utcEpoch);
}
