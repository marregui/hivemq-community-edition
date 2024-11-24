package com.hivemq.tk.ds;

public final class Hash {

    private Hash() {
    }

    public static int spread(final int h) {
        return (h ^ (h >>> 16)) & 0x7fffffff;
    }

}
