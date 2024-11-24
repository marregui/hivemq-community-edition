package com.hivemq.tk;

/**
 * A growable Utf8 buffer that can be written as a utf-8 sink and borrowed for writing in native code.
 * <p>
 * This is an interface abstraction of the sink, used by Prometheus scraping job. The "borrowable" part
 * is used in Ent by rust code.
 */
public interface BorrowableUtf8Sink extends Utf8Sink, BorrowableAsNativeByteSink {
}
