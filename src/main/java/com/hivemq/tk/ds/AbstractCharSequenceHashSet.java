package com.hivemq.tk.ds;

import com.hivemq.tk.str.Chars;
import com.hivemq.tk.str.Mutable;
import com.hivemq.tk.Numbers;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class AbstractCharSequenceHashSet implements Mutable {
    protected static final int MIN_INITIAL_CAPACITY = 16;
    protected static final CharSequence noEntryKey = null;
    protected final double loadFactor;
    protected int capacity;
    protected int free;
    protected CharSequence[] keys;
    protected int mask;

    public AbstractCharSequenceHashSet(int initialCapacity, double loadFactor) {
        if (loadFactor <= 0d || loadFactor >= 1d) {
            throw new IllegalArgumentException("0 < loadFactor < 1");
        }

        free = this.capacity =
                initialCapacity < MIN_INITIAL_CAPACITY ? MIN_INITIAL_CAPACITY : Numbers.ceilPow2(initialCapacity);
        this.loadFactor = loadFactor;
        int len = Numbers.ceilPow2((int) (this.capacity / loadFactor));
        keys = new CharSequence[len];
        mask = len - 1;
    }

    @Override
    public void clear() {
        Arrays.fill(keys, noEntryKey);
        free = capacity;
    }

    public boolean contains(@NotNull CharSequence key) {
        return keyIndex(key) < 0;
    }

    public boolean excludes(@NotNull CharSequence key) {
        return keyIndex(key) > -1;
    }

    /**
     * Returns the index of a free slot where this key can be placed.
     * Returns the negative index of the key if it's already present.
     *
     * @param key the key whose slot to look for
     * @return the index of a free slot where this key can be placed,
     *         or the negative index of the key if it's already present.
     */
    public int keyIndex(@NotNull CharSequence key) {
        int index = Hash.spread(Chars.hashCode(key)) & mask;
        if (keys[index] == noEntryKey) {
            return index;
        }
        if (Chars.equals(key, keys[index])) {
            return -index - 1;
        }
        return probe(key, index);
    }

    public int size() {
        return capacity - free;
    }

    private int probe(CharSequence key, int index) {
        do {
            index = (index + 1) & mask;
            if (keys[index] == noEntryKey) {
                return index;
            }
            if (Chars.equals(key, keys[index])) {
                return -index - 1;
            }
        } while (true);
    }

}
