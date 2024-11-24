package com.hivemq.tk;

import com.hivemq.util.Bytes;

public final class LogLevel {
    public static int ADVISORY = 16;
    public static String ADVISORY_HEADER = " A ";
    public static int CRITICAL = 8;
    public static String CRITICAL_HEADER = " C ";
    public static int DEBUG = 1;
    public static String DEBUG_HEADER = " D ";
    public static int ERROR = 4;
    public static String ERROR_HEADER = " E ";
    public static int INFO = 2;
    public static int ALL = DEBUG | INFO | ERROR | CRITICAL | ADVISORY;
    public static String INFO_HEADER = " I ";
    public static int MAX = Bytes.msb(LogLevel.ADVISORY) + 1;
    public static int MASK = ~(-1 << (MAX));

    private LogLevel() {
    }

    public static void init() {
            ADVISORY_HEADER = " ADVISORY ";
            CRITICAL_HEADER = " CRITICAL ";
            DEBUG_HEADER = " DEBUG ";
            ERROR_HEADER = " ERROR ";
            INFO_HEADER = " INFO ";
    }
}
