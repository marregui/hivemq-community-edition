package com.hivemq.tk.ds;

import com.hivemq.tk.str.Chars;
import com.hivemq.tk.Numbers;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CharSequenceIntHashMap extends AbstractCharSequenceHashSet {
    public static final int NO_ENTRY_VALUE = -1;
    private final ObjList<CharSequence> list;
    private final int noEntryValue;
    private int[] values;

    public CharSequenceIntHashMap() {
        this(8);
    }

    public CharSequenceIntHashMap(int initialCapacity) {
        this(initialCapacity, 0.4, NO_ENTRY_VALUE);
    }

    public CharSequenceIntHashMap(int initialCapacity, double loadFactor, int noEntryValue) {
        super(initialCapacity, loadFactor);
        this.noEntryValue = noEntryValue;
        this.list = new ObjList<>(capacity);
        values = new int[keys.length];
        clear();
    }

    @Override
    public final void clear() {
        super.clear();
        list.clear();
        Arrays.fill(values, noEntryValue);
    }

    public int get(@NotNull CharSequence key) {
        return valueAt(keyIndex(key));
    }

    public boolean put(@NotNull CharSequence key, int value) {
        return putAt(keyIndex(key), key, value);
    }

    public boolean putAt(int index, @NotNull CharSequence key, int value) {
        if (index < 0) {
            values[-index - 1] = value;
            return false;
        }
        final String keyString = Chars.toString(key);
        putAt0(index, keyString, value);
        list.add(keyString);
        return true;
    }

    public int valueAt(int index) {
        int index1 = -index - 1;
        return index < 0 ? values[index1] : noEntryValue;
    }

    private void putAt0(int index, CharSequence key, int value) {
        keys[index] = key;
        values[index] = value;
        if (--free == 0) {
            rehash();
        }
    }

    private void rehash() {
        int[] oldValues = values;
        CharSequence[] oldKeys = keys;
        int size = capacity - free;
        capacity = capacity * 2;
        free = capacity - size;
        mask = Numbers.ceilPow2((int) (capacity / loadFactor)) - 1;
        this.keys = new CharSequence[mask + 1];
        this.values = new int[mask + 1];
        for (int i = oldKeys.length - 1; i > -1; i--) {
            CharSequence key = oldKeys[i];
            if (key != null) {
                final int index = keyIndex(key);
                keys[index] = key;
                values[index] = oldValues[i];
            }
        }
    }

}
