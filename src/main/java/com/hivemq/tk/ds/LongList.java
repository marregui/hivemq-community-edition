package com.hivemq.tk.ds;

import com.hivemq.tk.*;
import org.jetbrains.annotations.NotNull;

public class LongList implements Mutable, Sinkable {
    private static final int DEFAULT_ARRAY_SIZE = 16;
    private static final long DEFAULT_NO_ENTRY_VALUE = -1L;
    private final long noEntryValue;
    private long[] data;
    private int pos = 0;

    public LongList() {
        this(DEFAULT_ARRAY_SIZE);
    }

    public LongList(int capacity) {
        this(capacity, DEFAULT_NO_ENTRY_VALUE);
    }

    public LongList(int capacity, long noEntryValue) {
        this.data = new long[capacity];
        this.noEntryValue = noEntryValue;
    }

    public void add(long value) {
        checkCapacity(pos + 1);
        data[pos++] = value;
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
                return scanDir == -1 ?
                        scrollUp(mid, midVal) :
                        scrollDown(mid, high, midVal);
            }
        }
        return scanDir == -1 ?
                scanUp(value, low, high + 1) :
                scanDown(value, low, high + 1);
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
