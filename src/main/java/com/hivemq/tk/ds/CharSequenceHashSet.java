package com.hivemq.tk.ds;

import com.hivemq.tk.CharSink;
import com.hivemq.tk.Chars;
import com.hivemq.tk.Numbers;
import com.hivemq.tk.Sinkable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CharSequenceHashSet extends AbstractCharSequenceHashSet implements Sinkable {

    private static final int MIN_INITIAL_CAPACITY = 16;
    private final ObjList<CharSequence> list;
    private boolean hasNull = false;

    public CharSequenceHashSet() {
        this(MIN_INITIAL_CAPACITY);
    }

    private CharSequenceHashSet(int initialCapacity) {
        this(initialCapacity, 0.4);
    }

    public CharSequenceHashSet(int initialCapacity, double loadFactor) {
        super(initialCapacity, loadFactor);
        list = new ObjList<>(free);
        clear();
    }

    /**
     * Adds key to hash set preserving key uniqueness.
     *
     * @param key immutable sequence of characters.
     * @return false if key is already in the set and true otherwise.
     */
    public boolean add(@Nullable CharSequence key) {
        if (key == null) {
            return addNull();
        }

        int index = keyIndex(key);
        if (index < 0) {
            return false;
        }

        addAt(index, key);
        return true;
    }

    public void addAt(int index, @NotNull CharSequence key) {
        final String s = Chars.toString(key);
        keys[index] = s;
        list.add(s);
        if (--free < 1) {
            rehash();
        }
    }

    public boolean addNull() {
        if (hasNull) {
            return false;
        }
        --free;
        hasNull = true;
        list.add(null);
        return true;
    }

    @Override
    public final void clear() {
        free = capacity;
        Arrays.fill(keys, null);
        list.clear();
        hasNull = false;
    }

    @Override
    public boolean contains(@Nullable CharSequence key) {
        return key == null ? hasNull : keyIndex(key) < 0;
    }

    @Override
    public boolean excludes(@Nullable CharSequence key) {
        return key == null ? !hasNull : keyIndex(key) > -1;
    }

    public CharSequence get(int index) {
        return list.getQuick(index);
    }

    @Override
    public void toSink(@NotNull CharSink<?> sink) {
        sink.put(list);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    private void rehash() {
        int newCapacity = capacity * 2;
        free = capacity = newCapacity;
        int len = Numbers.ceilPow2((int) (newCapacity / loadFactor));
        this.keys = new CharSequence[len];
        mask = len - 1;
        int n = list.size();
        free -= n;
        for (int i = 0; i < n; i++) {
            final CharSequence key = list.getQuick(i);
            keys[keyIndex(key)] = key;
        }
    }

}
