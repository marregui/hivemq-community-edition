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
package com.hivemq.persistence.local.xodus.bucket;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteOrder;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class BucketUtils {

    private static final long P1 = -7046029288634856825L;
    private static final long P2 = -4417276706812531889L;
    private static final long P3 = 1609587929392839161L;
    private static final long P4 = -8796714831421723037L;
    private static final long P5 = 2870177450012600261L;

    private static final Reader READER = ByteOrder.nativeOrder() == LITTLE_ENDIAN ? new Reader() {
        @Override
        public long i64(final @NotNull CharSequence cs, final long idx) {
            return BucketUtils.i64(cs, idx, 0, 1, 2, 3, 4, 0);
        }

        @Override
        public long u32(final @NotNull CharSequence cs, final long idx) {
            return BucketUtils.u32(cs, idx, 0, 1, 2, 0);
        }

        @Override
        public int u8(final @NotNull CharSequence cs, final long idx) {
            return BucketUtils.u8(cs, idx, ((int) idx & 1) << 3);
        }
    } : new Reader() {
        @Override
        public long i64(final @NotNull CharSequence cs, final long idx) {
            return BucketUtils.i64(cs, idx, 3, 2, 1, 0, 0, 1);
        }

        @Override
        public long u32(final @NotNull CharSequence cs, final long idx) {
            return BucketUtils.u32(cs, idx, 1, 0, 0, 1);
        }

        @Override
        public int u8(final @NotNull CharSequence cs, final long idx) {
            return BucketUtils.u8(cs, idx, (((int) idx & 1) ^ 1) << 3);
        }
    };

    public static int getBucket(@NotNull final String id, final int bucketSize) {
        final long length = id.length() * 2L;
        long hash;
        long idx = 0L;
        long remaining = length;
        if (remaining >= 32L) {
            @SuppressWarnings("NumericOverflow") long v1 = P1 + P2;
            long v2 = P2;
            long v3 = 0L;
            long v4 = -P1;
            do {
                v1 += READER.i64(id, idx) * P2;
                v1 = rotateLeft(v1, 31);
                v1 *= P1;
                v2 += READER.i64(id, idx + 8) * P2;
                v2 = rotateLeft(v2, 31);
                v2 *= P1;
                v3 += READER.i64(id, idx + 16) * P2;
                v3 = rotateLeft(v3, 31);
                v3 *= P1;
                v4 += READER.i64(id, idx + 24) * P2;
                v4 = rotateLeft(v4, 31);
                v4 *= P1;
                idx += 32;
                remaining -= 32;
            } while (remaining >= 32);
            hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);
            hash = hash(hash, v1, v2);
            hash = hash(hash, v3, v4);
        } else {
            hash = P5;
        }
        hash += length;
        while (remaining >= 8) {
            long k1 = READER.i64(id, idx);
            k1 *= P2;
            k1 = rotateLeft(k1, 31);
            k1 *= P1;
            hash ^= k1;
            hash = rotateLeft(hash, 27) * P1 + P4;
            idx += 8;
            remaining -= 8;
        }
        if (remaining >= 4) {
            hash ^= READER.u32(id, idx) * P1;
            hash = rotateLeft(hash, 23) * P2 + P3;
            idx += 4;
            remaining -= 4;
        }
        while (remaining != 0) {
            hash ^= READER.u8(id, idx) * P5;
            hash = rotateLeft(hash, 11) * P1;
            --remaining;
            ++idx;
        }
        hash ^= hash >>> 33;
        hash *= P2;
        hash ^= hash >>> 29;
        hash *= P3;
        hash ^= hash >>> 32;
        return Math.abs((int) (hash % bucketSize));
    }

    private static long hash(long hash, long v1, long v2) {
        v1 *= P2;
        v1 = rotateLeft(v1, 31);
        v1 *= P1;
        hash ^= v1;
        hash = hash * P1 + P4;
        v2 *= P2;
        v2 = rotateLeft(v2, 31);
        v2 *= P1;
        hash ^= v2;
        hash = hash * P1 + P4;
        return hash;
    }

    private static long rotateLeft(final long i, final int distance) {
        return (i << distance) | (i >>> -distance);
    }

    private static int idx(final long idx) {
        return (int) (idx >> 1);
    }

    private static long i64(
            final @NotNull CharSequence cs,
            final long idx,
            final int ch0Idx,
            final int ch1Idx,
            final int ch2Idx,
            final int ch3Idx,
            final int ch4Idx,
            final int delta) {
        final int base = idx(idx);
        if (0 == ((int) idx & 1)) {
            final long ch0 = cs.charAt(base + ch0Idx);
            final long ch1 = cs.charAt(base + ch1Idx);
            final long ch2 = cs.charAt(base + ch2Idx);
            final long ch3 = cs.charAt(base + ch3Idx);
            return ch0 | (ch1 << 16) | (ch2 << 32) | (ch3 << 48);
        } else {
            final long ch0 = cs.charAt(base + ch0Idx + delta) >>> 8;
            final long ch1 = cs.charAt(base + ch1Idx + delta);
            final long ch2 = cs.charAt(base + ch2Idx + delta);
            final long ch3 = cs.charAt(base + ch3Idx + delta);
            final long ch4 = cs.charAt(base + ch4Idx);
            return ch0 | (ch1 << 8) | (ch2 << 24) | (ch3 << 40) | (ch4 << 56);
        }
    }

    private static long u32(
            final @NotNull CharSequence cs,
            final long idx,
            final int ch0Idx,
            final int ch1Idx,
            final int ch2Idx,
            final int delta) {
        final int base = idx(idx);
        if (0 == ((int) idx & 1)) {
            final long ch0 = cs.charAt(base + ch0Idx);
            final long ch1 = cs.charAt(base + ch1Idx);
            return ch0 | (ch1 << 16);
        } else {
            final long ch0 = cs.charAt(base + ch0Idx + delta) >>> 8;
            final long ch1 = cs.charAt(base + ch1Idx + delta);
            final long ch2 = cs.charAt(base + ch2Idx) & 0xFF;
            return ch0 | (ch1 << 8) | (ch2 << 24);
        }
    }

    private static int u8(final @NotNull CharSequence cs, final long idx, final int shift) {
        return (cs.charAt(idx(idx)) >> shift) & 0xFF;
    }

    private interface Reader {
        long i64(final @NotNull CharSequence cs, final long idx);

        long u32(final @NotNull CharSequence cs, final long idx);

        int u8(final @NotNull CharSequence cs, final long idx);
    }
}
