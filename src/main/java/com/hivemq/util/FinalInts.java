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

import java.io.Serializable;
import java.util.*;


public final class FinalInts implements Serializable {
    public static final @NotNull FinalInts NONE = new FinalInts(new int[0], 0, 0);

    private final int[] ints;
    private final transient int start;
    private final int end;

    private FinalInts(int[] ints, int start, int end) {
        this.ints = ints;
        this.start = start;
        this.end = end;
    }

    public static @NotNull FinalInts of(final int first, final int... rest) {
        final int[] array = new int[rest.length + 1];
        array[0] = first;
        System.arraycopy(rest, 0, array, 1, rest.length);
        return new FinalInts(array, 0, array.length);
    }

    public static FinalInts copyOf(final @Nullable Collection<Integer> values) {
        return values == null || values.isEmpty() ? NONE : new FinalInts(toArray(values), 0, values.size());
    }

    public static int[] toArray(Collection<? extends Number> collection) {
        if (collection instanceof IntArrayAsList) {
            return ((IntArrayAsList) collection).toIntArray();
        }

        Object[] boxedArray = collection.toArray();
        int len = boxedArray.length;
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = ((Number) Objects.requireNonNull(boxedArray[i])).intValue();
        }
        return array;
    }

    public static FinalInts repeat(final int value, final int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        final int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = value;
        }
        return new FinalInts(array, 0, array.length);
    }

    public static Builder builder(int initialCapacity) {
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

    public int get(int index) {
        if (index < 0 || index >= end - start) {
            throw new IndexOutOfBoundsException();
        }
        return ints[start + index];
    }

    public int indexOf(int target) {
        for (int i = start; i < end; i++) {
            if (ints[i] == target) {
                return i - start;
            }
        }
        return -1;
    }

    public int lastIndexOf(int target) {
        for (int i = end - 1; i >= start; i--) {
            if (ints[i] == target) {
                return i - start;
            }
        }
        return -1;
    }

    public boolean contains(int target) {
        return indexOf(target) >= 0;
    }

    public FinalInts subArray(int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex < startIndex || endIndex > end - start) {
            throw new IndexOutOfBoundsException();
        }
        return startIndex == endIndex ? NONE : new FinalInts(ints, start + startIndex, start + endIndex);
    }

    private Spliterator.OfInt spliterator() {
        return Spliterators.spliterator(ints, start, end, Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }

    public List<Integer> asList() {
        return new ListView(this);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof FinalInts)) {
            return false;
        }
        FinalInts that = (FinalInts) object;
        if (this.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (this.get(i) != that.get(i)) {
                return false;
            }
        }
        return true;
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
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder(size() * 5); // rough estimate is fine
        builder.append('[').append(ints[start]);

        for (int i = start + 1; i < end; i++) {
            builder.append(", ").append(ints[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    private static class IntArrayAsList extends AbstractList<Integer> implements RandomAccess, Serializable {
        private static final long serialVersionUID = 0;
        final int[] array;
        final int start;
        final int end;

        IntArrayAsList(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        private static int indexOf(int[] array, int target, int start, int end) {
            for (int i = start; i < end; i++) {
                if (array[i] == target) {
                    return i;
                }
            }
            return -1;
        }

        private static int lastIndexOf(int[] array, int target, int start, int end) {
            for (int i = end - 1; i >= start; i--) {
                if (array[i] == target) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int size() {
            return end - start;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Integer get(int index) {
            if (index < 0 || index >= end - start) {
                throw new IndexOutOfBoundsException();
            }
            return array[start + index];
        }

        @Override
        public Spliterator.OfInt spliterator() {
            return Spliterators.spliterator(array, start, end, 0);
        }

        @Override
        public boolean contains(Object target) {
            // Overridden to prevent a ton of boxing
            return (target instanceof Integer) && indexOf(array, (Integer) target, start, end) != -1;
        }

        @Override
        public int indexOf(Object target) {
            // Overridden to prevent a ton of boxing
            if (target instanceof Integer) {
                int i = indexOf(array, (Integer) target, start, end);
                if (i >= 0) {
                    return i - start;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object target) {
            // Overridden to prevent a ton of boxing
            if (target instanceof Integer) {
                int i = lastIndexOf(array, (Integer) target, start, end);
                if (i >= 0) {
                    return i - start;
                }
            }
            return -1;
        }

        @Override
        public Integer set(int index, Integer element) {
            if (index < 0 || index >= end - start) {
                throw new IndexOutOfBoundsException();
            }
            int oldValue = array[start + index];
            array[start + index] = Objects.requireNonNull(element);
            return oldValue;
        }

        @Override
        public List<Integer> subList(int fromIndex, int toIndex) {
            if (fromIndex < 0 || toIndex < fromIndex || toIndex > end - start) {
                throw new IndexOutOfBoundsException();
            }
            if (fromIndex == toIndex) {
                return Collections.emptyList();
            }
            return new IntArrayAsList(array, start + fromIndex, start + toIndex);
        }

        @Override
        public boolean equals(@org.checkerframework.checker.nullness.qual.Nullable Object object) {
            if (object == this) {
                return true;
            }
            if (object instanceof IntArrayAsList) {
                IntArrayAsList that = (IntArrayAsList) object;
                int size = size();
                if (that.size() != size) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    if (array[start + i] != that.array[that.start + i]) {
                        return false;
                    }
                }
                return true;
            }
            return super.equals(object);
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (int i = start; i < end; i++) {
                result = 31 * result + array[i];
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(size() * 5);
            builder.append('[').append(array[start]);
            for (int i = start + 1; i < end; i++) {
                builder.append(", ").append(array[i]);
            }
            return builder.append(']').toString();
        }

        int[] toIntArray() {
            return Arrays.copyOfRange(array, start, end);
        }
    }

    public static final class Builder {
        private int[] array;
        private int size;

        private Builder(int initialCapacity) {
            array = new int[initialCapacity];
        }


        public Builder add(int value) {
            ensureSize(1);
            array[size] = value;
            size += 1;
            return this;
        }

        public Builder addAll(final @NotNull Collection<Integer> values) {
            ensureSize(values.size());
            for (final Integer value : values) {
                array[size++] = value;
            }
            return this;
        }

        public Builder addAll(final @NotNull FinalInts values) {
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

        public FinalInts build() {
            return size == 0 ? NONE : new FinalInts(array, 0, size);
        }
    }

    private static class ListView extends AbstractList<Integer> implements RandomAccess, Serializable {
        private final @NotNull FinalInts fints;

        private ListView(final @NotNull FinalInts fInts) {
            this.fints = fInts;
        }

        @Override
        public int size() {
            return fints.size();
        }

        @Override
        public @Nullable Integer get(final int index) {
            return fints.get(index);
        }

        @Override
        public boolean contains(Object target) {
            return indexOf(target) >= 0;
        }

        @Override
        public int indexOf(Object target) {
            return target instanceof Integer ? fints.indexOf((Integer) target) : -1;
        }

        @Override
        public int lastIndexOf(Object target) {
            return target instanceof Integer ? fints.lastIndexOf((Integer) target) : -1;
        }

        @Override
        public List<Integer> subList(int fromIndex, int toIndex) {
            return fints.subArray(fromIndex, toIndex).asList();
        }

        @Override
        public Spliterator<Integer> spliterator() {
            return fints.spliterator();
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (object instanceof ListView) {
                ListView that = (ListView) object;
                return this.fints.equals(that.fints);
            }
            // We could delegate to super now but it would still box too much
            if (!(object instanceof List)) {
                return false;
            }
            List<?> that = (List<?>) object;
            if (this.size() != that.size()) {
                return false;
            }
            int i = fints.start;
            // Since `that` is very likely RandomAccess we could avoid allocating this iterator...
            for (Object element : that) {
                if (!(element instanceof Integer) || fints.ints[i++] != (Integer) element) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return fints.hashCode();
        }

        @Override
        public String toString() {
            return fints.toString();
        }
    }
}
