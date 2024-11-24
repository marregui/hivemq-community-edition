package com.hivemq.tk.ds;

import com.hivemq.tk.CharSink;
import com.hivemq.tk.Mutable;
import com.hivemq.tk.Sinkable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

public class ObjList<T> implements Mutable, Sinkable {
    private static final int DEFAULT_ARRAY_SIZE = 16;
    private T[] buffer;
    private int pos = 0;

    @SuppressWarnings("unchecked")
    public ObjList() {
        this.buffer = (T[]) new Object[DEFAULT_ARRAY_SIZE];
    }

    @SuppressWarnings("unchecked")
    public ObjList(int capacity) {
        this.buffer = (T[]) new Object[Math.max(capacity, DEFAULT_ARRAY_SIZE)];
    }

    public void add(T value) {
        checkCapacity(pos + 1);
        buffer[pos++] = value;
    }

    public void addAll(ObjList<? extends T> that) {
        int n = that.size();
        checkCapacity(pos + n);
        for (int i = 0; i < n; i++) {
            buffer[pos++] = that.getQuick(i);
        }
    }

    @SuppressWarnings("unchecked")
    public void checkCapacity(int capacity) {
        int l = buffer.length;
        if (capacity > l) {
            int newCap = Math.max(l << 1, capacity);
            T[] buf = (T[]) new Object[newCap];
            System.arraycopy(buffer, 0, buf, 0, l);
            this.buffer = buf;
        }
    }

    @Override
    public void clear() {
        if (pos > 0) {
            Arrays.fill(buffer, null);
        }
        pos = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return this == that || that instanceof ObjList && equals((ObjList<?>) that);
    }

    public void extendAndSet(int index, T value) {
        checkCapacity(index + 1);
        if (index >= pos) {
            pos = index + 1;
        }
        buffer[index] = value;
    }

    public T get(int index) {
        if (index < pos) {
            return buffer[index];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Returns last element of the list or null if list is empty.
     *
     * @return last element of the list
     */
    public T getLast() {
        if (pos > 0) {
            return buffer[pos - 1];
        }
        return null;
    }

    /**
     * Returns element at the specified position. This method does not do
     * bounds check and may cause memory corruption if index is out of bounds.
     * Instead, the responsibility to check bounds is placed on application code,
     * which is often the case anyway, for example in indexed for() loop.
     *
     * @param index of the element
     * @return element at the specified position.
     */
    public T getQuick(int index) {
        assert index < pos : "index out of bounds, " + index + " >= " + pos;
        return buffer[index];
    }

    /**
     * Returns element at the specified position or null, if element index is
     * out of bounds. This is an alternative to throwing runtime exception or
     * doing preemptive check.
     *
     * @param index position of element
     * @return element at the specified position.
     */
    public T getQuiet(int index) {
        if (index < pos) {
            return buffer[index];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0, n = pos; i < n; i++) {
            T o = getQuick(i);
            hashCode = 31 * hashCode + (o == null ? 0 : o.hashCode());
        }
        return hashCode;
    }

    public int indexOf(Object o) {
        if (o == null) {
            return indexOfNull();
        } else {
            for (int i = 0, n = pos; i < n; i++) {
                if (o.equals(getQuick(i))) {
                    return i;
                }
            }
            return -1;
        }
    }

    public int indexOfNull() {
        for (int i = 0, n = pos; i < n; i++) {
            if (null == getQuick(i)) {
                return i;
            }
        }
        return -1;
    }

    public void remove(int index) {
        if (pos < 1 || index >= pos) {
            return;
        }
        int move = pos - index - 1;
        if (move > 0) {
            System.arraycopy(buffer, index + 1, buffer, index, move);
        }
        buffer[--pos] = null;
    }

    public int remove(Object o) {
        if (pos == 0) {
            return -1;
        }
        int index = indexOf(o);
        if (index > -1) {
            remove(index);
            return index;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return pos;
    }

    public void sort(Comparator<T> cmp) {
        sort(0, pos, cmp);
    }

    public void sort(int from, int to, Comparator<T> cmp) {
        Arrays.sort(buffer, from, to, cmp);
    }

    @Override
    public void toSink(@NotNull CharSink<?> sink) {
        toSink(sink, 0, size());
    }

    public void toSink(CharSink<?> sink, int from, int to) {
        sink.putAscii('[');
        for (int i = from; i < to; i++) {
            if (i > from) {
                sink.putAscii(',');
            }
            T obj = getQuick(i);
            if (obj instanceof Sinkable) {
                sink.put((Sinkable) obj);
            } else if (obj == null) {
                sink.putAscii("null");
            } else {
                sink.put(obj.toString());
            }
        }
        sink.putAscii(']');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.setLength(0);
        b.append('[');
        for (int i = 0, k = size(); i < k; i++) {
            if (i > 0) {
                b.append(',');
            }
            b.append(getQuick(i));
        }
        b.append(']');
        return b.toString();
    }

    private boolean equals(ObjList<?> that) {
        if (this.pos == that.pos) {
            for (int i = 0, n = pos; i < n; i++) {
                Object lhs = this.getQuick(i);
                if (lhs == null) {
                    if (that.getQuick(i) != null) {
                        return false;
                    }
                } else if (!lhs.equals(that.getQuick(i))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }
}
