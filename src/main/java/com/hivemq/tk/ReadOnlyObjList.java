package com.hivemq.tk;

public interface ReadOnlyObjList<T> {
    T get(int index);

    T getLast();

    T getQuick(int index);

    T getQuiet(int index);

    int indexOf(Object o);

    int size();
}
