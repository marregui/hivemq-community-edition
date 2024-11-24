package com.hivemq.tk.ds;

import com.hivemq.tk.Mutable;
import com.hivemq.tk.Numbers;

import java.util.Arrays;

public class IntStack implements Mutable {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MIN_INITIAL_CAPACITY = 8;
    private static final int noEntryValue = -1;
    private int[] elements;
    private int head;
    private int mask;
    private int tail;

    public IntStack() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public IntStack(int initialCapacity) {
        allocateElements(initialCapacity);
    }

    public void clear() {
        if (head != tail) {
            head = tail = 0;
            Arrays.fill(elements, noEntryValue);
        }
    }

    public void copyTo(IntStack there, int count) {
        int n = Math.min(count, size());
        while (n-- > 0) {
            there.push(pop());
        }
    }

    public boolean notEmpty() {
        return head != tail;
    }

    public int peek() {
        return elements[head];
    }

    public int pollLast() {
        final int[] es = elements;
        final int t;
        final int e = es[t = dec(tail)];
        tail = t;
        es[t] = noEntryValue;
        return e;
    }

    public int pop() {
        int h = head;
        int result = elements[h];
        if (result == noEntryValue) {
            return noEntryValue;
        }
        elements[h] = noEntryValue;
        head = (h + 1) & mask;
        return result;
    }

    public void push(int e) {
        elements[head = (head - 1) & mask] = e;
        if (head == tail) {
            doubleCapacity();
        }
    }

    public int size() {
        return (tail - head) & mask;
    }

    public void update(int e) {
        elements[head] = e;
    }

    private void allocateElements(int capacity) {
        capacity = capacity < MIN_INITIAL_CAPACITY ? MIN_INITIAL_CAPACITY : Numbers.ceilPow2(capacity);
        elements = new int[capacity];
        mask = capacity - 1;
        Arrays.fill(elements, noEntryValue);
    }

    private int dec(int i) {
        if (head != tail && --i < 0) {
            i = mask;
        }
        return i;
    }

    private void doubleCapacity() {
        assert head == tail;
        int h = head;
        int n = elements.length;
        int r = n - h;
        int newCapacity = n << 1;
        if (newCapacity < 0) {
            throw new IllegalStateException("Stack is too big");
        }
        int[] next = new int[newCapacity];
        System.arraycopy(elements, h, next, 0, r);
        System.arraycopy(elements, 0, next, r, h);
        Arrays.fill(next, r + h, newCapacity, noEntryValue);
        elements = next;
        head = 0;
        tail = n;
        mask = newCapacity - 1;
    }
}
