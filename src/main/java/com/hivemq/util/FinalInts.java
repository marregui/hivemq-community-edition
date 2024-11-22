/*
 * Copyright (C) 2017 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hivemq.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FinalInts {
    public static final @NotNull FinalInts NONE = new FinalInts(new int[0], 0, 0);

    private final int @NotNull [] ints;
    private final transient int start;
    private final int end;

    private FinalInts(final int @NotNull [] ints, final int start, final int end) {
        this.ints = ints;
        this.start = start;
        this.end = end;
    }

    public static @NotNull FinalInts of(final int value) {
        return new FinalInts(new int[]{value}, 0, 1);
    }

    public static @NotNull FinalInts of(final int first, final int @NotNull ... rest) {
        final int[] array = new int[rest.length + 1];
        array[0] = first;
        System.arraycopy(rest, 0, array, 1, rest.length);
        return new FinalInts(array, 0, array.length);
    }

    public static FinalInts repeat(final int value, final int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        final int[] array = new int[size];
        Arrays.fill(array, value);
        return new FinalInts(array, 0, array.length);
    }

    public static Builder builder(final int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        return new Builder(initialCapacity);
    }

    public static Builder builder() {
        return new Builder(10);
    }

    public int size() {
        return end - start;
    }

    public boolean isEmpty() {
        return end == start;
    }

    public int get(final int index) {
        if (index < 0 || index >= end - start) {
            throw new IndexOutOfBoundsException();
        }
        return ints[start + index];
    }

    public boolean contains(final int target) {
        for (int i = start; i < end; i++) {
            if (ints[i] == target) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (o == this) {
            return true;
        }
        final int size = end - start;
        if (o instanceof FinalInts) {
            final FinalInts that = (FinalInts) o;
            if (size != that.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (ints[start + i] != that.ints[that.start + i]) {
                    return false;
                }
            }
            return true;
        } else if (o instanceof List<?>) {
            final List<?> that = (List<?>) o;
            if (size != that.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                final Object v = that.get(i);
                if (!(v instanceof Number) || ((Number) v).intValue() != ints[start + i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (int i = start; i < end; i++) {
            hash *= 31;
            hash += ints[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        if (end == start) {
            return "[]";
        }
        final StringBuilder sb = new StringBuilder((end - start) * 5);
        sb.append('[').append(ints[start]);
        for (int i = start + 1; i < end; i++) {
            sb.append(',').append(ints[i]);
        }
        return sb.append(']').toString();
    }

    public static final class Builder {
        private int @NotNull [] array;
        private int size;

        private Builder(final int initialCapacity) {
            array = new int[initialCapacity];
        }

        public @NotNull Builder add(final int value) {
            ensureSize(1);
            array[size] = value;
            size += 1;
            return this;
        }

        public @NotNull Builder addAll(final @NotNull FinalInts values) {
            final int additional = values.size();
            ensureSize(additional);
            System.arraycopy(values.ints, values.start, array, size, additional);
            size += additional;
            return this;
        }

        private void ensureSize(final int additional) {
            final int required = size + additional;
            if (required < 0) {
                throw new IllegalArgumentException();
            }
            if (required > array.length) {
                int newSize = array.length + (array.length >> 1) + 1;
                if (newSize < required) {
                    newSize = Integer.highestOneBit(required - 1) << 1;
                }
                if (newSize < 0) {
                    newSize = Integer.MAX_VALUE;
                }
                if (newSize != array.length) {
                    final int[] newArray = new int[newSize];
                    System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newSize));
                    array = newArray;
                }
            }
        }

        public @NotNull FinalInts build() {
            return size == 0 ? NONE : new FinalInts(array, 0, size);
        }
    }
}
