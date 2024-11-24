package com.hivemq.tk.ds;

import com.hivemq.tk.CharSink;
import com.hivemq.tk.Numbers;
import com.hivemq.tk.Sinkable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class IntHashSet extends AbstractIntHashSet implements Sinkable {

    private static final int MIN_INITIAL_CAPACITY = 16;
    private final IntList list;

    public IntHashSet() {
        this(MIN_INITIAL_CAPACITY);
    }

    public IntHashSet(int initialCapacity) {
        this(initialCapacity, 0.4, noEntryKey);
    }

    public IntHashSet(int initialCapacity, double loadFactor, int noKeyValue) {
        super(initialCapacity, loadFactor, noKeyValue);
        list = new IntList(free);
        clear();
    }

    /**
     * Adds key to hash set preserving key uniqueness.
     *
     * @param key key to be added.
     * @return false if key is already in the set and true otherwise.
     */
    public boolean add(int key) {
        int index = keyIndex(key);
        if (index < 0) {
            return false;
        }

        addAt(index, key);
        return true;
    }

    public void addAt(int index, int key) {
        keys[index] = key;
        list.add(key);
        if (--free < 1) {
            rehash();
        }
    }

    public final void clear() {
        free = capacity;
        Arrays.fill(keys, noEntryKeyValue);
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntHashSet that = (IntHashSet) o;
        if (size() != that.size()) {
            return false;
        }
        for (int i = 0, n = list.size(); i < n; i++) {
            int key = list.getQuick(i);
            if (key != noEntryKeyValue && that.excludes(key)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0, n = keys.length; i < n; i++) {
            if (keys[i] != noEntryKeyValue) {
                hashCode += keys[i];
            }
        }
        return hashCode;
    }

    @Override
    public void toSink(@NotNull CharSink<?> sink) {
        list.toSink(sink, noEntryKeyValue);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    private void rehash() {
        int newCapacity = capacity * 2;
        free = capacity = newCapacity;
        int len = Numbers.ceilPow2((int) (newCapacity / loadFactor));
        keys = new int[len];
        Arrays.fill(keys, noEntryKeyValue);
        mask = len - 1;
        int n = list.size();
        free -= n;
        for (int i = 0; i < n; i++) {
            final int key = list.getQuick(i);
            keys[keyIndex(key)] = key;
        }
    }

}
