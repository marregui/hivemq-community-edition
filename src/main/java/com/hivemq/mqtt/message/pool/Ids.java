/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.mqtt.message.pool;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ids {

    public static final int MAX = 1 << 16;
    public static final int MIN = 1;

    private @NotNull Range root;

    public Ids() {
        root = new Range(MIN, MAX + 1, null);
    }

    public synchronized int lockId() throws UnavailableIdException {
        if (root.start == root.end) {
            throw new UnavailableIdException();
        }
        final int id = root.start++;
        if ((root.start == root.end) && (root.next != null)) {
            final Range ptr = root;
            root = root.next;
            ptr.next = null;
        }
        return id;
    }

    public synchronized void lockId(final int id) throws UnavailableIdException {
        if (id < MIN || id > MAX) {
            throw new IllegalArgumentException("id is out of range: " + id);
        }

        Range prev = null;
        for (Range ptr = root; ptr != null; prev = ptr, ptr = ptr.next) {
            if (id < ptr.start) {
                throw new UnavailableIdException(id);
            }
            if (id < ptr.end) {
                final int start = ptr.start;
                ptr.start = id + 1;
                if (start != id) {
                    final Range lo = new Range(start, id, ptr);
                    if (prev != null) {
                        prev.next = lo;
                    } else {
                        root = lo;
                    }
                }
                while ((root.start == root.end) && (root.next != null)) {
                    root = root.next;
                }
                return;
            }
        }
        throw new UnavailableIdException(id);
    }

    public void unlockId(final int id) {
        if (id < MIN || id > MAX) {
            throw new IllegalArgumentException("id is out of range: " + id);
        }
        synchronized (this) {
            Range current = root;
            if (id < current.start - 1) { // at least one element is between the returned and the next range
                root = new Range(id, id + 1, current);
                return;
            }
            Range prev = current;
            current = unlockId(current, id);
            while (current != null) {
                if (id < current.start - 1) {
                    prev.next = new Range(id, id + 1, current);
                    return;
                }
                prev = current;
                current = unlockId(current, id);
            }
        }
    }

    private static @Nullable Range unlockId(final @NotNull Range range, final int id) {
        if (id == range.start - 1) { // if the returned element is directly adjacent to the range (from below)
            range.start = id;
            return null;
        }
        if (id < range.end) { // the returned element is within the range, i.e. it has been freed already
            return null;
        }
        final Range next = range.next;
        Preconditions.checkState(next != null, "The id is greater than maxId. This must not happen and is a bug.");
        if (id == range.end) {
            range.end++;
            if (range.end == next.start) {
                range.end = next.end;
                range.next = next.next;
            }
            return null;
        }
        return next;
    }

    private static class Range {
        int start;
        int end;
        @Nullable Range next;

        Range(final int start, final int end, final @Nullable Range next) {
            this.start = start;
            this.end = end;
            this.next = next;
        }
    }
}
