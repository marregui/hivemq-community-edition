package com.hivemq.tk.ds;

public final class Hash {

    private Hash() {
    }

    public static int spread(final int h) {
        return (h ^ (h >>> 16)) & 0x7fffffff;
    }

    public static int hashLong32(long k) {
        return (int) hashLong64(k);
    }

    public static long hashLong64(long k) {
        return fmix64(k);
    }

    private static long fmix64(long h) {
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return h ^ (h >>> 33);
    }

}
