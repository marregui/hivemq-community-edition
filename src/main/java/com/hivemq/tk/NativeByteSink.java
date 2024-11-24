package com.hivemq.tk;

/**
 * A try-with-resource accessor to the `questdb_byte_sink_t` structure.
 * <p>
 * Note that the close method is simply meant to allow the owner object to
 * update its memory bookkeeping. The underlying memory is not released.
 */
public interface NativeByteSink extends QuietCloseable {
    /**
     * Get the raw pointer to the `questdb_byte_sink_t` C structure.
     */
    long ptr();
}
