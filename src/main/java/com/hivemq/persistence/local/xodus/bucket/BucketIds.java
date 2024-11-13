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

import static com.hivemq.persistence.local.xodus.bucket.Bucket.HASH0;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.HASH1;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.HASH2;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.HASH3;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.HASH4;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.hash;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.idx;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class BucketIds {

    private static final @NotNull Reader READER =
            ByteOrder.nativeOrder() == LITTLE_ENDIAN ? new LittleEndianReader() : new BigEndianReader();

    public static int getBucket(final @NotNull CharSequence id, final int bucketSize) {
        final long length = id.length() * 2L;
        long hash;
        long idx = 0L;
        long remaining = length;
        if (remaining >= 32L) {
            @SuppressWarnings("NumericOverflow") long v1 = HASH0 + HASH1;
            long v2 = HASH1;
            long v3 = 0L;
            long v4 = -HASH0;
            do {
                v1 += READER.i64(id, idx) * HASH1;
                v1 = (v1 << 31) | (v1 >>> -31);
                v1 *= HASH0;
                v2 += READER.i64(id, idx + 8) * HASH1;
                v2 = (v2 << 31) | (v2 >>> -31);
                v2 *= HASH0;
                v3 += READER.i64(id, idx + 16) * HASH1;
                v3 = (v3 << 31) | (v3 >>> -31);
                v3 *= HASH0;
                v4 += READER.i64(id, idx + 24) * HASH1;
                v4 = (v4 << 31) | (v4 >>> -31);
                v4 *= HASH0;
                idx += 32;
                remaining -= 32;
            } while (remaining >= 32);
            hash = ((v1 << 1) | (v1 >>> -1)) +
                    ((v2 << 7) | (v2 >>> -7)) +
                    ((v3 << 12) | (v3 >>> -12)) +
                    ((v4 << 18) | (v4 >>> -18));
            hash = hash(hash, v1, v2);
            hash = hash(hash, v3, v4);
        } else {
            hash = HASH4;
        }
        hash += length;
        while (remaining >= 8) {
            long k1 = READER.i64(id, idx);
            k1 *= HASH1;
            k1 = (k1 << 31) | (k1 >>> -31);
            k1 *= HASH0;
            hash ^= k1;
            hash = ((hash << 27) | (hash >>> -27)) * HASH0 + HASH3;
            idx += 8;
            remaining -= 8;
        }
        if (remaining >= 4) {
            hash ^= READER.u32(id, idx) * HASH0;
            hash = ((hash << 23) | (hash >>> -23)) * HASH1 + HASH2;
            idx += 4;
            remaining -= 4;
        }
        while (remaining != 0) {
            hash ^= READER.u8(id, idx) * HASH4;
            hash = ((hash << 11) | (hash >>> -11)) * HASH0;
            --remaining;
            ++idx;
        }
        hash ^= hash >>> 33;
        hash *= HASH1;
        hash ^= hash >>> 29;
        hash *= HASH2;
        hash ^= hash >>> 32;
        return Math.abs((int) (hash % bucketSize));
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
            return cs.charAt(base + ch0Idx) |
                    ((long) cs.charAt(base + ch1Idx) << 16) |
                    ((long) cs.charAt(base + ch2Idx) << 32) |
                    ((long) cs.charAt(base + ch3Idx) << 48);
        } else {
            return (long) (cs.charAt(base + ch0Idx + delta) >>> 8) |
                    ((long) cs.charAt(base + ch1Idx + delta) << 8) |
                    ((long) cs.charAt(base + ch2Idx + delta) << 24) |
                    ((long) cs.charAt(base + ch3Idx + delta) << 40) |
                    ((long) cs.charAt(base + ch4Idx) << 56);
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
            return cs.charAt(base + ch0Idx) | ((long) cs.charAt(base + ch1Idx) << 16);
        } else {
            return (cs.charAt(base + ch0Idx + delta) >>> 8) |
                    (cs.charAt(base + ch1Idx + delta) << 8) |
                    ((long) (cs.charAt(base + ch2Idx) & 0xFF) << 24);
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

    private static class LittleEndianReader implements Reader {
        @Override
        public long i64(final @NotNull CharSequence cs, final long idx) {
            return BucketIds.i64(cs, idx, 0, 1, 2, 3, 4, 0);
        }

        @Override
        public long u32(final @NotNull CharSequence cs, final long idx) {
            return BucketIds.u32(cs, idx, 0, 1, 2, 0);
        }

        @Override
        public int u8(final @NotNull CharSequence cs, final long idx) {
            return BucketIds.u8(cs, idx, ((int) idx & 1) << 3);
        }

    }

    private static class BigEndianReader implements Reader {
        @Override
        public long i64(final @NotNull CharSequence cs, final long idx) {
            return BucketIds.i64(cs, idx, 3, 2, 1, 0, 0, 1);
        }

        @Override
        public long u32(final @NotNull CharSequence cs, final long idx) {
            return BucketIds.u32(cs, idx, 1, 0, 0, 1);
        }

        @Override
        public int u8(final @NotNull CharSequence cs, final long idx) {
            return BucketIds.u8(cs, idx, (((int) idx & 1) ^ 1) << 3);
        }
    }
}
