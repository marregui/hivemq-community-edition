package com.hivemq.tk.str;

import org.jetbrains.annotations.NotNull;

public interface Sinkable {
    void toSink(@NotNull CharSink<?> sink);
}
