package com.hivemq.tk.ds;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface ImmutableIterator<T> extends Iterator<T>, Iterable<T> {

    @Override
    @NotNull
    default Iterator<T> iterator() {
        return this;
    }
}
