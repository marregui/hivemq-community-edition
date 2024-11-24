package com.hivemq.tk;

public class TransitionRule {
    public static final int STANDARD = 1;
    public static final int UTC = 0;
    public static final int WALL = 2;
    public int dom;
    public int dow;
    public int hour;
    public boolean midnightEOD;
    public int minute;
    public int month;
    public int offsetAfter;
    public int offsetBefore;
    public int second;
    public int standardOffset;
    public int timeDef;
}
