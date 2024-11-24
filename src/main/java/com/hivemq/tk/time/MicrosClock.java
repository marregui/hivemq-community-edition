package com.hivemq.tk.time;

import org.jetbrains.annotations.NotNull;

public class MicrosClock {
    public static final @NotNull MicrosClock INSTANCE = new MicrosClock();

    public long getTicks() {
        return Timestamps.currentTimeMicros();
    }
}


