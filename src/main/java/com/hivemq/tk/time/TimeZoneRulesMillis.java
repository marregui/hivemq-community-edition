package com.hivemq.tk.time;


import com.hivemq.tk.AbstractTimeZoneRules;

import java.time.zone.ZoneRules;

public class TimeZoneRulesMillis extends AbstractTimeZoneRules {
    public TimeZoneRulesMillis(ZoneRules rules) {
        super(rules, Dates.SECOND_MILLIS);
    }

    @Override
    public long getNextDST(long utcEpoch) {
        return 0;
    }

    @Override
    protected long addDays(long epoch, int days) {
        return Dates.addDays(epoch, days);
    }

    @Override
    protected int getDaysPerMonth(int month, boolean leapYear) {
        return Dates.getDaysPerMonth(month, leapYear);
    }

    @Override
    protected int getYear(long epoch) {
        return Dates.getYear(epoch);
    }

    @Override
    protected boolean isLeapYear(int year) {
        return Dates.isLeapYear(year);
    }

    @Override
    protected long nextOrSameDayOfWeek(long epoch, int dow) {
        return Dates.nextOrSameDayOfWeek(epoch, dow);
    }

    @Override
    protected long previousOrSameDayOfWeek(long epoch, int dow) {
        return Dates.previousOrSameDayOfWeek(epoch, dow);
    }

    @Override
    protected long toEpoch(int year, boolean leapYear, int month, int day, int hour, int min) {
        return Dates.toMillis(year, leapYear, month, day, hour, min);
    }
}
