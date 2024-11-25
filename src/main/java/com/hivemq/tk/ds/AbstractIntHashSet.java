package com.hivemq.tk.ds;

import com.hivemq.tk.Mutable;
import com.hivemq.tk.Numbers;

import java.util.Arrays;

public abstract class AbstractIntHashSet implements Mutable {
    protected static final int MIN_INITIAL_CAPACITY = 16;
    protected static final int noEntryKey = -1;
    protected final double loadFactor;
    protected final int noEntryKeyValue;
    protected int capacity;
    protected int free;
    protected int[] keys;
    protected int mask;

    public AbstractIntHashSet(int initialCapacity, double loadFactor, int noKeyValue) {
        if (loadFactor <= 0d || loadFactor >= 1d) {
            throw new IllegalArgumentException("0 < loadFactor < 1");
        }
        this.noEntryKeyValue = noKeyValue;
        free = this.capacity = Math.max(initialCapacity, MIN_INITIAL_CAPACITY);
        this.loadFactor = loadFactor;
        int len = Numbers.ceilPow2((int) (this.capacity / loadFactor));
        keys = new int[len];
        mask = len - 1;
    }

    @Override
    public void clear() {
        Arrays.fill(keys, noEntryKeyValue);
        free = capacity;
    }

    public boolean excludes(int key) {
        return keyIndex(key) > -1;
    }

    public int keyIndex(int key) {
        int index = key & mask;
        if (keys[index] == noEntryKeyValue) {
            return index;
        }
        if (key == keys[index]) {
            return -index - 1;
        }
        return probe(key, index);
    }

    public int remove(int key) {
        int index = keyIndex(key);
        if (index < 0) {
            removeAt(index);
            return -index - 1;
        }
        return -1;
    }

    public void removeAt(int index) {
        if (index < 0) {
            int from = -index - 1;
            erase(from);
            free++;

            // after we have freed up a slot
            // consider non-empty keys directly below
            // they may have been a direct hit but because
            // directly hit slot wasn't empty these keys would
            // have moved.
            //
            // After slot if freed these keys require re-hash
            from = (from + 1) & mask;
            for (
                    int key = keys[from];
                    key != noEntryKeyValue;
                    from = (from + 1) & mask, key = keys[from]
            ) {
                int idealHit = key & mask;
                if (idealHit != from) {
                    int to;
                    if (keys[idealHit] != noEntryKeyValue) {
                        to = probe(key, idealHit);
                    } else {
                        to = idealHit;
                    }

                    if (to > -1) {
                        move(from, to);
                    }
                }
            }
        }
    }

    public int size() {
        return capacity - free;
    }

    private int probe(int key, int index) {
        do {
            index = (index + 1) & mask;
            if (keys[index] == noEntryKeyValue) {
                return index;
            }
            if (key == keys[index]) {
                return -index - 1;
            }
        } while (true);
    }

    abstract protected void erase(int index);

    abstract protected void move(int from, int to);
}
