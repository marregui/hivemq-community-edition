package com.hivemq.tk;

public interface LongVec {

    long getQuick(int index);

    LongVec newInstance();

    void setQuick(int index, long value);
}
