package com.hivemq.tk.time;

import com.hivemq.tk.*;
import com.hivemq.tk.ds.BinarySearch;
import com.hivemq.tk.ds.LongList;
import com.hivemq.tk.ds.ObjList;

import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneRules;

public abstract class AbstractTimeZoneRules implements TimeZoneRules {
    public static final long LAST_RULES = Unsafe.fieldOffset(ZoneRules.class, "lastRules");
    public static final long SAVING_INSTANT_TRANSITION =
            Unsafe.fieldOffset(ZoneRules.class, "savingsInstantTransitions");
    public static final long STANDARD_OFFSETS = Unsafe.fieldOffset(ZoneRules.class, "standardOffsets");
    public static final long WALL_OFFSETS = Unsafe.fieldOffset(ZoneRules.class, "wallOffsets");
    private final long cutoffTransition;
    private final long firstWall;
    private final LongList historicTransitions = new LongList();
    private final long lastWall;
    private final long multiplier;
    private final int ruleCount;
    private final ObjList<TransitionRule> rules;
    private final long standardOffset;
    private final int[] wallOffsets;

    public AbstractTimeZoneRules(ZoneRules rules, long multiplier) {
        this.multiplier = multiplier;
        final long[] savingsInstantTransition = (long[]) Unsafe.UNSAFE.getObject(rules, SAVING_INSTANT_TRANSITION);

        if (savingsInstantTransition.length == 0) {
            ZoneOffset[] standardOffsets = (ZoneOffset[]) Unsafe.UNSAFE.getObject(rules, STANDARD_OFFSETS);
            standardOffset = standardOffsets[0].getTotalSeconds() * multiplier;
        } else {
            standardOffset = Long.MIN_VALUE;
            for (int i = 0, n = savingsInstantTransition.length; i < n; i++) {
                historicTransitions.add(savingsInstantTransition[i] * multiplier);
            }
        }

        cutoffTransition = historicTransitions.getLast();

        ZoneOffsetTransitionRule[] lastRules = (ZoneOffsetTransitionRule[]) Unsafe.UNSAFE.getObject(rules, LAST_RULES);
        this.rules = new ObjList<>(lastRules.length);
        for (int i = 0, n = lastRules.length; i < n; i++) {
            ZoneOffsetTransitionRule zr = lastRules[i];
            TransitionRule tr = new TransitionRule();
            tr.offsetBefore = zr.getOffsetBefore().getTotalSeconds();
            tr.offsetAfter = zr.getOffsetAfter().getTotalSeconds();
            tr.standardOffset = zr.getStandardOffset().getTotalSeconds();
            tr.dow = zr.getDayOfWeek() == null ? -1 : zr.getDayOfWeek().getValue();
            tr.dom = zr.getDayOfMonthIndicator();
            tr.month = zr.getMonth().getValue();
            tr.midnightEOD = zr.isMidnightEndOfDay();
            tr.hour = zr.getLocalTime().getHour();
            tr.minute = zr.getLocalTime().getMinute();
            tr.second = zr.getLocalTime().getSecond();
            switch (zr.getTimeDefinition()) {
                case UTC:
                    tr.timeDef = TransitionRule.UTC;
                    break;
                case STANDARD:
                    tr.timeDef = TransitionRule.STANDARD;
                    break;
                default:
                    tr.timeDef = TransitionRule.WALL;
                    break;
            }
            this.rules.add(tr);
        }

        this.ruleCount = lastRules.length;

        ZoneOffset[] wallOffsets = (ZoneOffset[]) Unsafe.UNSAFE.getObject(rules, WALL_OFFSETS);
        this.wallOffsets = new int[wallOffsets.length];
        for (int i = 0, n = wallOffsets.length; i < n; i++) {
            this.wallOffsets[i] = wallOffsets[i].getTotalSeconds();
        }
        this.firstWall = this.wallOffsets[0] * multiplier;
        this.lastWall = this.wallOffsets[wallOffsets.length - 1] * multiplier;
    }

    @Override
    public long getOffset(long utcEpoch, int year, boolean leap) {
        if (standardOffset != Long.MIN_VALUE) {
            return standardOffset;
        }

        if (ruleCount > 0 && utcEpoch > cutoffTransition) {
            // offset from rules
            int offsetBefore;
            int offsetAfter = 0;

            for (int i = 0; i < ruleCount; i++) {
                TransitionRule zr = rules.getQuick(i);
                offsetBefore = zr.offsetBefore;
                offsetAfter = zr.offsetAfter;

                int dom = zr.dom;
                int month = zr.month;

                int dow = zr.dow;
                long date;
                if (dom < 0) {
                    date = toEpoch(year, leap, month, getDaysPerMonth(month, leap) + 1 + dom, zr.hour, zr.minute) +
                            zr.second * multiplier;
                    if (dow > -1) {
                        date = previousOrSameDayOfWeek(date, dow);
                    }
                } else {
                    assert month > 0;
                    date = toEpoch(year, leap, month, dom, zr.hour, zr.minute) + zr.second * multiplier;
                    if (dow > -1) {
                        date = nextOrSameDayOfWeek(date, dow);
                    }
                }

                if (zr.midnightEOD) {
                    date = addDays(date, 1);
                }

                switch (zr.timeDef) {
                    case TransitionRule.UTC:
                        date += (offsetBefore - ZoneOffset.UTC.getTotalSeconds()) * multiplier;
                        break;
                    case TransitionRule.STANDARD:
                        date += (offsetBefore - zr.standardOffset) * multiplier;
                        break;
                    default:  // WALL
                        break;
                }

                // go back to epoch epoch
                date -= offsetBefore * multiplier;

                if (utcEpoch < date) {
                    return offsetBefore * multiplier;
                }
            }
            return offsetAfter * multiplier;
        }

        if (utcEpoch > cutoffTransition) {
            return lastWall;
        }
        return offsetFromHistory(utcEpoch);
    }

    @Override
    public long getOffset(long utcEpoch) {
        final int y = getYear(utcEpoch);
        return getOffset(utcEpoch, y, isLeapYear(y));
    }

    private long offsetFromHistory(long epoch) {
        int index = historicTransitions.binarySearch(epoch, BinarySearch.SCAN_UP);
        if (index == -1) {
            return firstWall;
        }

        if (index < 0) {
            index = -index - 2;
        }
        return wallOffsets[index + 1] * multiplier;
    }

    abstract protected long addDays(long epoch, int days);

    abstract protected int getDaysPerMonth(int month, boolean leapYear);

    abstract protected int getYear(long epoch);

    abstract protected boolean isLeapYear(int year);

    abstract protected long nextOrSameDayOfWeek(long epoch, int dow);

    abstract protected long previousOrSameDayOfWeek(long epoch, int dow);

    abstract protected long toEpoch(int year, boolean leapYear, int month, int day, int hour, int min);
}
