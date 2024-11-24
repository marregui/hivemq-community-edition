package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public interface Sinkable {
    void toSink(@NotNull CharSink<?> sink);
}
