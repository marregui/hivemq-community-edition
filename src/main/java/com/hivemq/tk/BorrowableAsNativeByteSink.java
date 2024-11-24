package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

/**
 * Implement to temporarily expose a {@link NativeByteSink} to the caller.
 */
public interface BorrowableAsNativeByteSink {
    @NotNull
    NativeByteSink borrowDirectByteSink();
}
