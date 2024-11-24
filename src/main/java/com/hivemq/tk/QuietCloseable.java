package com.hivemq.tk;

import java.io.Closeable;

public interface QuietCloseable extends Closeable {

    @Override
    void close();
}
