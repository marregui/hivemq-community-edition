package com.hivemq.tk.ds;

import com.hivemq.tk.ImmutableIterator;
import com.hivemq.tk.Mutable;
import com.hivemq.tk.Numbers;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class ObjIntHashMap<K> implements Iterable<ObjIntHashMap.Entry<K>>, Mutable {
    private static final int MIN_INITIAL_CAPACITY = 16;
    private static final Object noEntryValue = new Object();
    private final EntryIterator iterator = new EntryIterator();
    private final double loadFactor;
    private final int noKeyValue;
    private int capacity;
    private int free;
    private K[] keys;
    private int mask;
    private int[] values;

    public ObjIntHashMap() {
        this(8);
    }

    private ObjIntHashMap(int initialCapacity) {
        this(initialCapacity, 0.3, -1);
    }

    public ObjIntHashMap(int initialCapacity, double loadFactor, int noKeyValue) {
        assert loadFactor > 0 && loadFactor < 1.0;
        this.capacity = Math.max(initialCapacity, MIN_INITIAL_CAPACITY);
        this.loadFactor = loadFactor;
        this.noKeyValue = noKeyValue;
        keys = createKeys();
        values = new int[keys.length];
        mask = keys.length - 1;
        clear();
    }

    @Override
    public final void clear() {
        if (free != capacity) {
            free = capacity;
            Arrays.fill(keys, noEntryValue);
        }
    }

    @Override
    @NotNull
    public Iterator<Entry<K>> iterator() {
        iterator.index = 0;
        return iterator;
    }

    public int keyIndex(K key) {
        int index = Hash.spread(key.hashCode()) & mask;

        final K kv = keys[index];
        if (kv == noEntryValue) {
            return index;
        }

        if (kv == key || key.equals(kv)) {
            return -index - 1;
        }

        return probe(key, index);
    }

    public void put(K key, int value) {
        putAt(keyIndex(key), key, value);
    }

    public void putAt(int index, K key, int value) {
        if (index < 0) {
            values[-index - 1] = value;
            return;
        }
        putAt0(index, key, value);
    }

    public int valueAt(int index) {
        int index1 = -index - 1;
        return index < 0 ? values[index1] : noKeyValue;
    }

    @SuppressWarnings("unchecked")
    private K[] createKeys() {
        return (K[]) new Object[Numbers.ceilPow2((int) (this.capacity / this.loadFactor))];
    }

    private int probe(K key, int index) {
        do {
            index = (index + 1) & mask;
            final K kv = keys[index];
            if (kv == noEntryValue) {
                return index;
            }
            if (kv == key || key.equals(kv)) {
                return -index - 1;
            }
        } while (true);
    }

    private void putAt0(int index, K key, int value) {
        keys[index] = key;
        values[index] = value;
        if (--free == 0) {
            rehash();
        }
    }

    @SuppressWarnings({"unchecked"})
    private void rehash() {
        free = capacity = this.capacity * 2;
        int[] oldValues = values;
        K[] oldKeys = keys;
        this.keys = (K[]) new Object[Numbers.ceilPow2(Numbers.ceilPow2((int) (this.capacity / loadFactor)))];
        this.values = new int[keys.length];
        Arrays.fill(keys, noEntryValue);
        mask = keys.length - 1;

        for (int i = oldKeys.length; i-- > 0; ) {
            if (oldKeys[i] != noEntryValue) {
                put(oldKeys[i], oldValues[i]);
            }
        }
    }

    public static class Entry<V> {
    }

    public class EntryIterator implements ImmutableIterator<Entry<K>> {

        private final Entry<K> entry = new Entry<>();
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < values.length && (keys[index] != noEntryValue || scan());
        }

        @Override
        public Entry<K> next() {
            index++;
            return entry;
        }

        private boolean scan() {
            do {
                index++;
            } while (index < values.length && keys[index] == noEntryValue);

            return index < values.length;
        }
    }
}
