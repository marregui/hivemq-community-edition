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

import static com.hivemq.persistence.local.xodus.bucket.Bucket.N0;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.N1;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.N2;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.N3;
import static com.hivemq.persistence.local.xodus.bucket.Bucket.N4;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

class NumericHash {

    private static final @NotNull Reader reader =
            ByteOrder.nativeOrder() == LITTLE_ENDIAN ? new LittleEndianReader() : new BigEndianReader();

    static int hash(final long id, final int modulo) {
        if (id == 0) {
            return 0;
        }

        // convert the id to the equivalent char[], e.g. -12 -> char[]{'-', '1', '2'}
        final int sign;
        final long uid;
        final int extra;
        if (id < 0) {
            uid = -id;
            sign = -1;
            extra = 2;
        } else {
            sign = 1;
            uid = id;
            extra = 1;
        }
        final char[] ca = new char[(int) Math.log10(uid) + extra];
        int i = ca.length - 1;
        long rem = uid % 10;
        long div = uid / 10;
        while (div > 0) {
            ca[i--] = (char) (48 + rem);
            rem = (int) (div % 10);
            div = (int) (div / 10);
        }
        ca[i] = (char) (48 + rem);
        if (sign < 0) {
            ca[--i] = '-';
        }

        final long length = ca.length * 2L;
        long hash;
        long idx = 0L;
        long remaining = length;
        if (remaining >= 32L) {
            @SuppressWarnings("NumericOverflow") long h0 = N0 + N1;
            long h1 = N1;
            long h2 = 0L;
            long h3 = -N0;
            do {
                h0 += reader.i64(ca, idx) * N1;
                h0 = (h0 << 31) | (h0 >>> -31);
                h0 *= N0;
                h1 += reader.i64(ca, idx + 8) * N1;
                h1 = (h1 << 31) | (h1 >>> -31);
                h1 *= N0;
                h2 += reader.i64(ca, idx + 16) * N1;
                h2 = (h2 << 31) | (h2 >>> -31);
                h2 *= N0;
                h3 += reader.i64(ca, idx + 24) * N1;
                h3 = (h3 << 31) | (h3 >>> -31);
                h3 *= N0;
                idx += 32;
                remaining -= 32;
            } while (remaining >= 32);
            hash = ((h0 << 1) | (h0 >>> -1)) +
                    ((h1 << 7) | (h1 >>> -7)) +
                    ((h2 << 12) | (h2 >>> -12)) +
                    ((h3 << 18) | (h3 >>> -18));
            hash = Bucket.hash(hash, h0, h1);
            hash = Bucket.hash(hash, h2, h3);
        } else {
            hash = N4;
        }
        hash += length;
        while (remaining >= 8) {
            long h = reader.i64(ca, idx);
            h *= N1;
            h = (h << 31) | (h >>> -31);
            h *= N0;
            hash ^= h;
            hash = ((hash << 27) | (hash >>> -27)) * N0 + N3;
            idx += 8;
            remaining -= 8;
        }
        if (remaining >= 4) {
            hash ^= reader.u32(ca, idx) * N0;
            hash = ((hash << 23) | (hash >>> -23)) * N1 + N2;
            idx += 4;
            remaining -= 4;
        }
        while (remaining != 0) {
            hash ^= reader.u8(ca, idx) * N4;
            hash = ((hash << 31) | (hash >>> -31)) * N0;
            --remaining;
            ++idx;
        }
        hash ^= hash >>> 33;
        hash *= N1;
        hash ^= hash >>> 29;
        hash *= N2;
        hash ^= hash >>> 32;
        return Math.abs((int) (hash % modulo));
    }

    private static long i64(
            final char @NotNull [] ca,
            final long idx,
            final int ch0Idx,
            final int ch1Idx,
            final int ch2Idx,
            final int ch3Idx,
            final int ch4Idx,
            final int delta) {
        final int base = (int) (idx >> 1);
        if (0 == ((int) idx & 1)) {
            return ca[base + ch0Idx] |
                    ((long) ca[base + ch1Idx] << 16) |
                    ((long) ca[base + ch2Idx] << 32) |
                    ((long) ca[base + ch3Idx] << 48);
        } else {
            return (long) (ca[base + ch0Idx + delta] >>> 8) |
                    ((long) ca[base + ch1Idx + delta] << 8) |
                    ((long) ca[base + ch2Idx + delta] << 24) |
                    ((long) ca[base + ch3Idx + delta] << 40) |
                    ((long) ca[base + ch4Idx] << 56);
        }
    }

    private static long u32(
            final char @NotNull [] cs,
            final long idx,
            final int ch0Idx,
            final int ch1Idx,
            final int ch2Idx,
            final int delta) {
        final int base = (int) (idx >> 1);
        if (0 == ((int) idx & 1)) {
            return cs[base + ch0Idx] | ((long) cs[base + ch1Idx] << 16);
        } else {
            return (cs[base + ch0Idx + delta] >>> 8) |
                    (cs[base + ch1Idx + delta] << 8) |
                    ((long) (cs[base + ch2Idx] & 0xFF) << 24);
        }
    }

    private static int u8(final char @NotNull [] cs, final long idx, final int shift) {
        return (cs[(int) (idx >> 1)] >> shift) & 0xFF;
    }

    private interface Reader {
        long i64(final char @NotNull [] cs, final long idx);

        long u32(final char @NotNull [] cs, final long idx);

        int u8(final char @NotNull [] cs, final long idx);
    }

    private static class LittleEndianReader implements Reader {
        @Override
        public long i64(final char @NotNull [] cs, final long idx) {
            return NumericHash.i64(cs, idx, 0, 1, 2, 3, 4, 0);
        }

        @Override
        public long u32(final char @NotNull [] cs, final long idx) {
            return NumericHash.u32(cs, idx, 0, 1, 2, 0);
        }

        @Override
        public int u8(final char @NotNull [] cs, final long idx) {
            return NumericHash.u8(cs, idx, ((int) idx & 1) << 3);
        }
    }

    private static class BigEndianReader implements Reader {
        @Override
        public long i64(final char @NotNull [] cs, final long idx) {
            return NumericHash.i64(cs, idx, 3, 2, 1, 0, 0, 1);
        }

        @Override
        public long u32(final char @NotNull [] cs, final long idx) {
            return NumericHash.u32(cs, idx, 1, 0, 0, 1);
        }

        @Override
        public int u8(final char @NotNull [] cs, final long idx) {
            return NumericHash.u8(cs, idx, (((int) idx & 1) ^ 1) << 3);
        }
    }
}
