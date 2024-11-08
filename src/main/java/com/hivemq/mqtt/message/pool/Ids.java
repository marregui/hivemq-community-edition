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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ids {

    public static final int MAX = 1 << 16;
    public static final int MIN = 1;

    private @NotNull Range root;

    public Ids() {
        root = new Range(MIN, MAX + 1, null);
    }

    private static @Nullable Range unlockId(final @NotNull Range range, final int id) {
        if (id == range.startInc - 1) { // if the returned element is directly adjacent to the range (from below)
            range.startInc = id;
            return null;
        }
        if (id < range.endExcl) { // the returned element is within the range, i.e. it has been freed already
            return null;
        }
        final Range next = range.next;
        if (next == null) {
            throw new IllegalStateException("id is greater than max");
        }
        if (id == range.endExcl) {
            range.endExcl++;
            if (range.endExcl == next.startInc) {
                range.endExcl = next.endExcl;
                range.next = next.next;
            }
            return null;
        }
        return next;
    }

    public synchronized int lockId() throws UnavailableIdException {
        if (root.startInc == root.endExcl) {
            throw new UnavailableIdException();
        }
        final int id = root.startInc;
        root.startInc++;
        if ((root.startInc == root.endExcl) && (root.next != null)) {
            final Range ptr = root;
            root = root.next;
            ptr.next = null;
        }
        return id;
    }

    public void lockId(final int id) throws UnavailableIdException {
        if (id < MIN || id > MAX) {
            throw new IllegalArgumentException("id is out of range: " + id);
        }
        synchronized (this) {
            Range prev = null;
            for (Range ptr = root; ptr != null; prev = ptr, ptr = ptr.next) {
                if (id < ptr.startInc) {
                    throw new UnavailableIdException(id);
                }
                if (id < ptr.endExcl) {
                    final int start = ptr.startInc;
                    ptr.startInc = id + 1;
                    if (start != id) {
                        final Range lo = new Range(start, id, ptr);
                        if (prev != null) {
                            prev.next = lo;
                        } else {
                            root = lo;
                        }
                    }
                    while ((root.startInc == root.endExcl) && (root.next != null)) {
                        root = root.next;
                    }
                    return;
                }
            }
        }
        throw new UnavailableIdException(id);
    }

    public void unlockId(final int id) {
        if (id < MIN || id > MAX) {
            throw new IllegalArgumentException("id is out of range: " + id);
        }
        synchronized (this) {
            Range ptr = root;
            if (id < ptr.startInc - 1) { // at least one element is between the returned and the next range
                root = new Range(id, id + 1, ptr);
                return;
            }
            Range prev = ptr;
            ptr = unlockId(ptr, id);
            while (ptr != null) {
                if (id < ptr.startInc - 1) {
                    prev.next = new Range(id, id + 1, ptr);
                    return;
                }
                prev = ptr;
                ptr = unlockId(ptr, id);
            }
        }
    }

    private static class Range {
        int startInc;
        int endExcl;
        @Nullable Range next;

        Range(final int startInc, final int endExcl, final @Nullable Range next) {
            this.startInc = startInc;
            this.endExcl = endExcl;
            this.next = next;
        }
    }
}
