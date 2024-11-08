package com.hivemq.persistence.payload;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LongIntMap implements Iterable<LongIntMap.Entry> {
    private static final long EMPTY_KEY = 0L;
    private static final long REMOVED_KEY = 1L;
    private static final int INITIAL_LINEAR_PROBE = 4;

    private long @NotNull [] keys = new long[16];
    private int @NotNull [] values = new int[16];
    private int occupiedWithData;
    private int occupiedWithSentinels;
    private @Nullable Sentinel sentinel;

    private static boolean isNonSentinel(final long key) {
        return EMPTY_KEY != key && REMOVED_KEY != key;
    }

    private static long longSpreadOne(final long code) {
        long code1 = code;
        code1 ^= code1 >>> 28;
        code1 *= -4254747342703917655L;
        code1 ^= code1 >>> 43;
        code1 *= -908430792394475837L;
        code1 ^= code1 >>> 23;
        return code1;
    }

    private static long longSpreadTwo(final long code) {
        long code1 = code;
        code1 ^= code1 >>> 23;
        code1 *= -6261870919139520145L;
        code1 ^= code1 >>> 39;
        code1 *= 2747051607443084853L;
        code1 ^= code1 >>> 37;
        return code1;
    }

    public int size() {
        return occupiedWithData + (sentinel == null ? 0 : sentinel.size());
    }

    public boolean hasKey(final long key) {
        if (EMPTY_KEY == key) {
            return sentinel != null && sentinel.containsEmpty;
        }
        if (REMOVED_KEY == key) {
            return sentinel != null && sentinel.containsRemoved;
        }
        return keys[probe(key)] == key;
    }

    public int get(final long key) {
        return getIfAbsent(key, 0);
    }

    public int getIfAbsent(final long key, final int ifAbsent) {
        if (EMPTY_KEY == key) {
            return sentinel != null && sentinel.containsEmpty ? sentinel.empty : ifAbsent;
        }
        if (REMOVED_KEY == key) {
            return sentinel != null && sentinel.containsRemoved ? sentinel.removed : ifAbsent;
        }
        if (occupiedWithSentinels == 0) {
            int index = mask((int) key);
            for (int i = 0; i < INITIAL_LINEAR_PROBE; i++) {
                final long keyAtIndex = keys[index];
                if (keyAtIndex == key) {
                    return values[index];
                }
                if (keyAtIndex == EMPTY_KEY) {
                    return ifAbsent;
                }
                index = (index + 1) & (keys.length - 1);
            }
            return get(key, probeTwo(key, -1), ifAbsent);
        }
        return get(key, probe(key), ifAbsent);
    }

    public int getOrThrow(final long key) {
        if (EMPTY_KEY == key) {
            if (sentinel == null || !sentinel.containsEmpty) {
                throw new IllegalStateException("key: " + key);
            }
            return sentinel.empty;
        }
        if (REMOVED_KEY == key) {
            if (sentinel == null || !sentinel.containsRemoved) {
                throw new IllegalStateException("key: " + key);
            }
            return sentinel.removed;
        }
        final int index = probe(key);
        if (isNonSentinel(keys[index])) {
            return values[index];
        }
        throw new IllegalStateException("key: " + key);
    }

    public void put(final long key, final int value) {
        if (EMPTY_KEY == key) {
            if (sentinel == null) {
                sentinel = new Sentinel();
            }
            sentinel.containsEmpty = true;
            sentinel.empty = value;
            return;
        }

        if (REMOVED_KEY == key) {
            if (sentinel == null) {
                sentinel = new Sentinel();
            }
            sentinel.containsRemoved = true;
            sentinel.removed = value;
            return;
        }

        final int index = probe(key);
        final long keyAtIndex = keys[index];
        if (keyAtIndex == key) {
            values[index] = value;
        } else {
            if (REMOVED_KEY == keys[index]) {
                occupiedWithSentinels--;
            }
            keys[index] = key;
            values[index] = value;
            occupiedWithData++;
            final int max = keys.length >> 1;
            if (occupiedWithData + occupiedWithSentinels > max) {
                int newCapacity = newCapacity(max);
                if (occupiedWithSentinels > 0 && (max >> 1) + (max >> 2) < occupiedWithData) {
                    newCapacity <<= 1;
                }
                final int oldLength = keys.length;
                final long[] old = keys;
                final int[] oldValues = values;
                keys = new long[newCapacity];
                values = new int[newCapacity];
                occupiedWithData = 0;
                occupiedWithSentinels = 0;
                for (int i = 0; i < oldLength; i++) {
                    if (isNonSentinel(old[i])) {
                        put(old[i], oldValues[i]);
                    }
                }
            }
        }
    }

    public void remove(final long key) {
        if (EMPTY_KEY == key) {
            if (sentinel == null || !sentinel.containsEmpty) {
                return;
            }
            if (sentinel.containsRemoved) {
                sentinel.containsEmpty = false;
                sentinel.empty = 0;
            } else {
                sentinel = null;
            }
            return;
        }
        if (REMOVED_KEY == key) {
            if (sentinel == null || !sentinel.containsRemoved) {
                return;
            }
            if (sentinel.containsEmpty) {
                sentinel.containsRemoved = false;
                sentinel.removed = 0;
            } else {
                sentinel = null;
            }
            return;
        }
        final int index = probe(key);
        if (keys[index] == key) {
            keys[index] = REMOVED_KEY;
            values[index] = 0;
            occupiedWithData--;
            occupiedWithSentinels++;
        }
    }

    public void clear() {
        sentinel = null;
        occupiedWithData = 0;
        occupiedWithSentinels = 0;
        Arrays.fill(keys, EMPTY_KEY);
        Arrays.fill(values, 0);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LongIntMap)) {
            return false;
        }
        final LongIntMap that = (LongIntMap) obj;
        if (size() != that.size()) {
            return false;
        }
        if (sentinel == null) {
            if (that.hasKey(EMPTY_KEY) || that.hasKey(REMOVED_KEY)) {
                return false;
            }
        } else {
            if (sentinel.containsEmpty &&
                    (!that.hasKey(EMPTY_KEY) || sentinel.empty != that.getOrThrow(EMPTY_KEY))) {
                return false;
            }
            if (sentinel.containsRemoved &&
                    (!that.hasKey(REMOVED_KEY) || sentinel.removed != that.getOrThrow(REMOVED_KEY))) {
                return false;
            }
        }
        for (int i = 0; i < keys.length; i++) {
            final long key = keys[i];
            if (isNonSentinel(key) && (!that.hasKey(key) || values[i] != that.getOrThrow(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (sentinel != null) {
            if (sentinel.containsEmpty) {
                result += Long.hashCode(EMPTY_KEY) ^ sentinel.empty;
            }
            if (sentinel.containsRemoved) {
                result += Long.hashCode(REMOVED_KEY) ^ sentinel.removed;
            }
        }
        for (int i = 0; i < keys.length; i++) {
            if (isNonSentinel(keys[i])) {
                result += Long.hashCode(keys[i]) ^ values[i];
            }
        }
        return result;
    }

    @Override
    public @NotNull String toString() {
        final StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (sentinel != null) {
            if (sentinel.containsEmpty) {
                sb.append(EMPTY_KEY).append('=').append(sentinel.empty);
                first = false;
            }
            if (sentinel.containsRemoved) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(REMOVED_KEY).append('=').append(sentinel.removed);
                first = false;
            }
        }
        for (int i = 0; i < keys.length; i++) {
            final long key = keys[i];
            if (isNonSentinel(key)) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(key).append('=').append(values[i]);
                first = false;
            }
        }
        return sb.append('}').toString();
    }

    private int get(final long key, final int index, final int ifAbsent) {
        return (keys[index] == key) ? values[index] : ifAbsent;
    }

    private int newCapacity(final int max) {
        final int n = (occupiedWithData + 1) << 1;
        return Math.max(max, n > 1 ? Integer.highestOneBit(n - 1) << 1 : 1);
    }

    private int probe(final long element) {
        final int index = mask((int) element);
        long key = keys[index];
        if (key == element || key == EMPTY_KEY) {
            return index;
        }
        int removedIndex = key == REMOVED_KEY ? index : -1;

        for (int i = 1; i < INITIAL_LINEAR_PROBE; i++) {
            final int nextIndex = (index + i) & (keys.length - 1);
            key = keys[nextIndex];
            if (key == element) {
                return nextIndex;
            }
            if (key == EMPTY_KEY) {
                return removedIndex == -1 ? nextIndex : removedIndex;
            }
            if (key == REMOVED_KEY && removedIndex == -1) {
                removedIndex = nextIndex;
            }
        }
        return probeTwo(element, removedIndex);
    }

    private int probeTwo(final long element, int removedIndex) {
        final int index = mask((int) longSpreadTwo(element));

        for (int i = 0; i < INITIAL_LINEAR_PROBE; i++) {
            final int nextIndex = (index + i) & (keys.length - 1);
            final long key = keys[nextIndex];
            if (key == element) {
                return nextIndex;
            }
            if (key == EMPTY_KEY) {
                return removedIndex == -1 ? nextIndex : removedIndex;
            }
            if (key == REMOVED_KEY && removedIndex == -1) {
                removedIndex = nextIndex;
            }
        }
        final int spreadTwo = (int) Long.reverse(longSpreadTwo(element)) | 1;
        int nextIndex = (int) longSpreadOne(element);
        while (true) {
            nextIndex = mask(nextIndex + spreadTwo);
            final long key = keys[nextIndex];
            if (key == element) {
                return nextIndex;
            }
            if (key == EMPTY_KEY) {
                return removedIndex == -1 ? nextIndex : removedIndex;
            }
            if (key == REMOVED_KEY && removedIndex == -1) {
                removedIndex = nextIndex;
            }
        }
    }

    private int mask(final int spread) {
        return spread & (keys.length - 1);
    }

    @Override
    public @NotNull Iterator<Entry> iterator() {
        return new Iterator<>() {
            private int seenCount;
            private int index;
            private boolean handledEmpty;
            private boolean handledRemoved;

            @Override
            public @NotNull Entry next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                seenCount++;
                if (!handledEmpty) {
                    handledEmpty = true;
                    if (hasKey(EMPTY_KEY)) {
                        return new Entry(EMPTY_KEY, sentinel.empty);
                    }
                }
                if (!handledRemoved) {
                    handledRemoved = true;
                    if (hasKey(REMOVED_KEY)) {
                        return new Entry(REMOVED_KEY, sentinel.removed);
                    }
                }
                while (!isNonSentinel(keys[index])) {
                    index++;
                }
                final Entry result = new Entry(keys[index], values[index]);
                index++;
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return seenCount != size();
            }
        };
    }

    public static class Entry implements Comparable<Entry> {
        private final long longV;
        private final int intV;

        private Entry(final long longV, final int intV) {
            this.longV = longV;
            this.intV = intV;
        }

        public long getLong() {
            return longV;
        }

        public int getInt() {
            return intV;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Entry)) {
                return false;
            }
            final Entry that = (Entry) o;
            return (longV == that.longV) && (intV == that.intV);
        }

        @Override
        public int hashCode() {
            return 29 * Long.hashCode(longV) + intV;
        }

        @Override
        public int compareTo(final @NotNull Entry that) {
            final int cmp = Long.compare(longV, that.longV);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(intV, that.intV);
        }
    }

    private static class Sentinel {
        private boolean containsEmpty;
        private boolean containsRemoved;
        private int empty;
        private int removed;

        private int size() {
            return (containsEmpty ? 1 : 0) + (containsRemoved ? 1 : 0);
        }
    }
}
