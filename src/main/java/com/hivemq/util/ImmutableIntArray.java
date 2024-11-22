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

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public final class ImmutableIntArray implements Serializable {
    public static final @NotNull ImmutableIntArray EMPTY = new ImmutableIntArray(new int[0]);

    private final int[] array;
    private final transient int start;
    private final int end;

    private ImmutableIntArray(int[] array) {
        this(array, 0, array.length);
    }

    private ImmutableIntArray(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    public static @NotNull ImmutableIntArray of(final int first, final int... rest) {
        final int[] array = new int[rest.length + 1];
        array[0] = first;
        System.arraycopy(rest, 0, array, 1, rest.length);
        return new ImmutableIntArray(array);
    }

    public static ImmutableIntArray copyOf(Collection<Integer> values) {
        return values.isEmpty() ? EMPTY : new ImmutableIntArray(Ints.toArray(values));
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

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return end == start;
    }

    public int get(int index) {
        Preconditions.checkElementIndex(index, length());
        return array[start + index];
    }

    public int indexOf(int target) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i - start;
            }
        }
        return -1;
    }

    public int lastIndexOf(int target) {
        for (int i = end - 1; i >= start; i--) {
            if (array[i] == target) {
                return i - start;
            }
        }
        return -1;
    }

    public boolean contains(int target) {
        return indexOf(target) >= 0;
    }

    public int[] toArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    public ImmutableIntArray subArray(int startIndex, int endIndex) {
        Preconditions.checkPositionIndexes(startIndex, endIndex, length());
        return startIndex == endIndex ? EMPTY : new ImmutableIntArray(array, start + startIndex, start + endIndex);
    }

    private Spliterator.OfInt spliterator() {
        return Spliterators.spliterator(array, start, end, Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }

    public List<Integer> asList() {
        return new AsList(this);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof ImmutableIntArray)) {
            return false;
        }
        ImmutableIntArray that = (ImmutableIntArray) object;
        if (this.length() != that.length()) {
            return false;
        }
        for (int i = 0; i < length(); i++) {
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
            hash += Ints.hashCode(array[i]);
        }
        return hash;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder(length() * 5); // rough estimate is fine
        builder.append('[').append(array[start]);

        for (int i = start + 1; i < end; i++) {
            builder.append(", ").append(array[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    public ImmutableIntArray trimmed() {
        return isPartialView() ? new ImmutableIntArray(toArray()) : this;
    }

    private boolean isPartialView() {
        return start > 0 || end < array.length;
    }

    Object writeReplace() {
        return trimmed();
    }

    Object readResolve() {
        return isEmpty() ? EMPTY : this;
    }

    public static final class Builder {
        private int[] array;
        private int count = 0; // <= array.length

        Builder(int initialCapacity) {
            array = new int[initialCapacity];
        }

        private static int expandedCapacity(int oldCapacity, int minCapacity) {
            if (minCapacity < 0) {
                throw new AssertionError("cannot store more than MAX_VALUE elements");
            }
            int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
            if (newCapacity < minCapacity) {
                newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
            }
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE; // guaranteed to be >= newCapacity
            }
            return newCapacity;
        }

        public Builder add(int value) {
            ensureRoomFor(1);
            array[count] = value;
            count += 1;
            return this;
        }

        public Builder addAll(Iterable<Integer> values) {
            if (values instanceof Collection) {
                return addAll((Collection<Integer>) values);
            }
            for (Integer value : values) {
                add(value);
            }
            return this;
        }

        public Builder addAll(Collection<Integer> values) {
            ensureRoomFor(values.size());
            for (Integer value : values) {
                array[count++] = value;
            }
            return this;
        }

        public Builder addAll(ImmutableIntArray values) {
            ensureRoomFor(values.length());
            System.arraycopy(values.array, values.start, array, count, values.length());
            count += values.length();
            return this;
        }

        private void ensureRoomFor(int numberToAdd) {
            int newCount = count + numberToAdd; // TODO(kevinb): check overflow now?
            if (newCount > array.length) {
                array = Arrays.copyOf(array, expandedCapacity(array.length, newCount));
            }
        }

        @CheckReturnValue
        public ImmutableIntArray build() {
            return count == 0 ? EMPTY : new ImmutableIntArray(array, 0, count);
        }
    }

    private static class AsList extends AbstractList<Integer> implements RandomAccess, Serializable {
        private final @NotNull ImmutableIntArray parent;

        private AsList(final @NotNull ImmutableIntArray parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.length();
        }

        @Override
        public @Nullable Integer get(final int index) {
            return parent.get(index);
        }

        @Override
        public boolean contains(Object target) {
            return indexOf(target) >= 0;
        }

        @Override
        public int indexOf(Object target) {
            return target instanceof Integer ? parent.indexOf((Integer) target) : -1;
        }

        @Override
        public int lastIndexOf(Object target) {
            return target instanceof Integer ? parent.lastIndexOf((Integer) target) : -1;
        }

        @Override
        public List<Integer> subList(int fromIndex, int toIndex) {
            return parent.subArray(fromIndex, toIndex).asList();
        }

        @Override
        public Spliterator<Integer> spliterator() {
            return parent.spliterator();
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (object instanceof AsList) {
                AsList that = (AsList) object;
                return this.parent.equals(that.parent);
            }
            // We could delegate to super now but it would still box too much
            if (!(object instanceof List)) {
                return false;
            }
            List<?> that = (List<?>) object;
            if (this.size() != that.size()) {
                return false;
            }
            int i = parent.start;
            // Since `that` is very likely RandomAccess we could avoid allocating this iterator...
            for (Object element : that) {
                if (!(element instanceof Integer) || parent.array[i++] != (Integer) element) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return parent.hashCode();
        }

        @Override
        public String toString() {
            return parent.toString();
        }
    }
}
