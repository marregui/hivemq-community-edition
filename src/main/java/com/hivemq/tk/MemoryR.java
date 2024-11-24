package com.hivemq.tk;

import java.io.Closeable;

// readable
public interface MemoryR extends Closeable {

    long addressOf(long offset);

    @Override
    void close();

    void extend(long size);

    BinarySequence getBin(long offset);

    long getBinLen(long offset);

    boolean getBool(long offset);

    byte getByte(long offset);

    char getChar(long offset);

    default DirectUtf8Sequence getDirectVarcharA(long offset, int size, boolean ascii) {
        throw new UnsupportedOperationException();
    }

    default DirectUtf8Sequence getDirectVarcharB(long offset, int size, boolean ascii) {
        throw new UnsupportedOperationException();
    }

    double getDouble(long offset);

    float getFloat(long offset);

    int getIPv4(long offset);

    int getInt(long offset);

    long getLong(long offset);

    void getLong256(long offset, CharSink<?> sink);

    long getPageAddress(int pageIndex);

    int getPageCount();

    long getPageSize();

    short getShort(long offset);

    CharSequence getStrA(long offset);

    CharSequence getStrB(long offset);

    int getStrLen(long offset);

    long offsetInPage(long offset);

    int pageIndex(long offset);

    long size();
}
