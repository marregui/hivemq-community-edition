package com.hivemq.tk.time;


import java.time.zone.ZoneRules;

public class TimeZoneRulesMicros extends AbstractTimeZoneRules {
    public TimeZoneRulesMicros(ZoneRules rules) {
        super(rules, Timestamps.SECOND_MICROS);
    }

    @Override
    protected long addDays(long epoch, int days) {
        return Timestamps.addDays(epoch, days);
    }

    @Override
    protected int getDaysPerMonth(int month, boolean leapYear) {
        return Timestamps.getDaysPerMonth(month, leapYear);
    }

    @Override
    protected int getYear(long epoch) {
        return Timestamps.getYear(epoch);
    }

    @Override
    protected boolean isLeapYear(int year) {
        return Timestamps.isLeapYear(year);
    }

    @Override
    protected long nextOrSameDayOfWeek(long epoch, int dow) {
        return Timestamps.nextOrSameDayOfWeek(epoch, dow);
    }

    @Override
    protected long previousOrSameDayOfWeek(long epoch, int dow) {
        return Timestamps.previousOrSameDayOfWeek(epoch, dow);
    }

    @Override
    protected long toEpoch(int year, boolean leapYear, int month, int day, int hour, int min) {
        return Timestamps.toMicros(year, leapYear, month, day, hour, min);
    }
}
