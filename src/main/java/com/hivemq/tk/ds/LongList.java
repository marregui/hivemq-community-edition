package com.hivemq.tk.ds;

import com.hivemq.tk.*;
import org.jetbrains.annotations.NotNull;

public class LongList implements Mutable, Sinkable, LongVec {
    private static final int DEFAULT_ARRAY_SIZE = 16;
    private static final long DEFAULT_NO_ENTRY_VALUE = -1L;
    private final long noEntryValue;
    private final int initialCapacity;
    private long[] data;
    private int pos = 0;

    public LongList() {
        this(DEFAULT_ARRAY_SIZE);
    }

    public LongList(int capacity) {
        this(capacity, DEFAULT_NO_ENTRY_VALUE);
    }

    public LongList(int capacity, long noEntryValue) {
        this.initialCapacity = capacity;
        this.data = new long[capacity];
        this.noEntryValue = noEntryValue;
    }

    public LongList(LongList other) {
        this.initialCapacity = Math.max(other.size(), DEFAULT_ARRAY_SIZE);
        this.data = new long[initialCapacity];
        setPos(other.size());
        System.arraycopy(other.data, 0, this.data, 0, pos);
        this.noEntryValue = other.noEntryValue;
    }

    public LongList(long[] other) {
        this.initialCapacity = other.length;
        this.data = new long[initialCapacity];
        setPos(other.length);
        System.arraycopy(other, 0, this.data, 0, pos);
        this.noEntryValue = DEFAULT_NO_ENTRY_VALUE;
    }

    public void add(long value) {
        checkCapacity(pos + 1);
        data[pos++] = value;
    }

    public void add(long value, long... rest) {
        checkCapacity(pos + 1 + rest.length);
        data[pos++] = value;
        for (int i = 0; i < rest.length; i++) {
            data[pos++] = rest[i];
        }
    }

    public int indexOf(long o) {
        for (int i = 0, n = pos; i < n; i++) {
            if (o == getQuick(i)) {
                return i;
            }
        }
        return -1;
    }

    public void restoreInitialCapacity() {
        data = new long[initialCapacity];
        pos = 0;
    }

    public int binarySearchBlock(int shl, long value, int scanDir) {
        // Binary searches using 2^shl blocks
        // e.g. when shl == 2
        // this method treats 4 longs as 1 entry
        // taking first long for the comparisons
        // and ignoring the other 3 values.

        // This is useful when list is a dictionary where first long is a key
        // and subsequent X (1, 3, 7 etc.) values are the value of the dictionary.

        // this is the same algorithm as implemented in C (util.h)
        // template<class T, class V>
        // inline int64_t binary_search(T *data, V value, int64_t low, int64_t high, int32_t scan_dir)
        // please ensure these implementations are in sync

        return binarySearchBlock(0, shl, value, scanDir);
    }


    public int binarySearchBlock(int offset, int shl, long value, int scanDir) {
        int low = offset >> shl;
        int high = (pos - 1) >> shl;
        while (high - low > 65) {
            final int mid = (low + high) >>> 1;
            final long midVal = data[mid << shl];

            if (midVal < value) {
                low = mid;
            } else if (midVal > value) {
                high = mid - 1;
            } else {
                // In case of multiple equal values, find the first
                return scanDir == BinarySearch.SCAN_UP ?
                        scrollUpBlock(shl, mid, midVal) :
                        scrollDownBlock(shl, mid, high, midVal);
            }
        }
        return scanDir == BinarySearch.SCAN_UP ?
                scanUpBlock(shl, value, low, high + 1) :
                scanDownBlock(shl, value, low, high + 1);
    }

    private int scanUpBlock(int shl, long value, int low, int high) {
        for (int i = low; i < high; i++) {
            long that = data[i << shl];
            if (that == value) {
                return i << shl;
            }
            if (that > value) {
                return -((i << shl) + 1);
            }
        }
        return -((high << shl) + 1);
    }

    private int scanDownBlock(int shl, long v, int low, int high) {
        for (int i = high - 1; i >= low; i--) {
            long that = data[i << shl];
            if (that == v) {
                return i << shl;
            }
            if (that < v) {
                return -(((i + 1) << shl) + 1);
            }
        }
        return -((low << shl) + 1);
    }

    private int scrollDownBlock(int shl, int low, int high, long value) {
        do {
            if (low < high) {
                low++;
            } else {
                return low << shl;
            }
        } while (data[low << shl] == value);
        return (low - 1) << shl;
    }

    private int scrollUpBlock(int shl, int high, long value) {
        do {
            if (high > 0) {
                high--;
            } else {
                return 0;
            }
        } while (data[high << shl] == value);
        return (high + 1) << shl;
    }

    public void remove(long v) {
        int index = indexOf(v);
        if (index > -1) {
            removeIndex(index);
        }
    }

    public void removeIndex(int index) {
        if (pos < 1 || index >= pos) {
            return;
        }
        int move = pos - index - 1;
        if (move > 0) {
            System.arraycopy(data, index + 1, data, index, move);
        }
        data[--pos] = noEntryValue;
    }

    public int capacity() {
        return data.length;
    }

    public void sort() {
        LongSort.sort(this, 0, size() - 1);
    }

    public void insertFromSource(int index, LongList src, int srcLo, int srcHi) {
        assert index > -1 && index < pos + 1 && srcLo > -1 && srcHi - 1 < src.size();
        int len = srcHi - srcLo;
        if (len > 0) {
            insert(index, len);
            System.arraycopy(src.data, srcLo, data, index, len);
        }
    }

    public void insert(int index, int length) {
        checkCapacity(pos + length);
        if (pos > index) {
            System.arraycopy(data, index, data, index + length, pos - index);
        }
        pos += length;
    }

    public int binarySearch(long value, int scanDir) {

        // this is the same algorithm as implemented in C (util.h)
        // template<class T, class V>
        // inline int64_t binary_search(T *data, V value, int64_t low, int64_t high, int32_t scan_dir)
        // please ensure these implementations are in sync

        int low = 0;
        int high = pos - 1;
        while (high - low > 65) {
            final int mid = (low + high) >>> 1;
            final long midVal = data[mid];

            if (midVal < value) {
                low = mid;
            } else if (midVal > value) {
                high = mid - 1;
            } else {
                // In case of multiple equal values, find the first
                return scanDir == -1 ? scrollUp(mid, midVal) : scrollDown(mid, high, midVal);
            }
        }
        return scanDir == -1 ? scanUp(value, low, high + 1) : scanDown(value, low, high + 1);
    }

    public void checkCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Negative capacity. Integer overflow may be?");
        }

        int l = data.length;
        if (capacity > l) {
            int newCap = Math.max(l << 1, capacity);
            long[] buf = new long[newCap];
            System.arraycopy(data, 0, buf, 0, l);
            this.data = buf;
        }
    }

    public void clear() {
        pos = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return this == that || that instanceof LongList && equals((LongList) that);
    }

    public long get(int index) {
        if (index < pos) {
            return data[index];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Returns last element of the list or null if list is empty.
     *
     * @return last element of the list
     */
    public long getLast() {
        if (pos > 0) {
            return data[pos - 1];
        }
        return noEntryValue;
    }

    /**
     * Returns element at the specified position. This method does not do
     * bounds check and may cause memory corruption if index is out of bounds.
     * Instead the responsibility to check bounds is placed on application code,
     * which is often the case anyway, for example in indexed for() loop.
     *
     * @param index of the element
     * @return element at the specified position.
     */
    public long getQuick(int index) {
        return data[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        long hashCode = 1;
        for (int i = 0, n = pos; i < n; i++) {
            long v = getQuick(i);
            hashCode = 31 * hashCode + (v == noEntryValue ? 0 : v);
        }
        return (int) hashCode;
    }

    public LongList newInstance() {
        LongList newList = new LongList(size());
        newList.setPos(pos);
        return newList;
    }

    public final void setPos(int pos) {
        checkCapacity(pos);
        this.pos = pos;
    }

    public void setQuick(int index, long value) {
        assert index < pos;
        data[index] = value;
    }

    public int size() {
        return pos;
    }

    @Override
    public void toSink(@NotNull CharSink<?> sink) {
        sink.putAscii('[');
        for (int i = 0, k = pos; i < k; i++) {
            if (i > 0) {
                sink.putAscii(',');
            }
            sink.put(get(i));
        }
        sink.putAscii(']');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final Utf16Sink sb = Misc.getThreadLocalSink();
        toSink(sb);
        return sb.toString();
    }

    private boolean equals(LongList that) {
        if (this.pos != that.pos) {
            return false;
        }
        if (this.noEntryValue != that.noEntryValue) {
            return false;
        }
        for (int i = 0, n = pos; i < n; i++) {
            if (this.getQuick(i) != that.getQuick(i)) {
                return false;
            }
        }
        return true;
    }

    private int scanDown(long v, int low, int high) {
        for (int i = high - 1; i >= low; i--) {
            long that = data[i];
            if (that == v) {
                return i;
            }
            if (that < v) {
                return -(i + 2);
            }
        }
        return -(low + 1);
    }

    private int scanUp(long value, int low, int high) {
        for (int i = low; i < high; i++) {
            long that = data[i];
            if (that == value) {
                return i;
            }
            if (that > value) {
                return -(i + 1);
            }
        }
        return -(high + 1);
    }

    private int scrollDown(int low, int high, long value) {
        do {
            if (low < high) {
                low++;
            } else {
                return low;
            }
        } while (data[low] == value);
        return low - 1;
    }

    private int scrollUp(int high, long value) {
        do {
            if (high > 0) {
                high--;
            } else {
                return 0;
            }
        } while (data[high] == value);
        return high + 1;
    }

}
