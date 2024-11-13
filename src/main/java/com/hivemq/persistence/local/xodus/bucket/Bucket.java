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
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;

import java.util.concurrent.atomic.AtomicBoolean;


public class Bucket {

    // https://github.com/Cyan4973/xxHash/blob/dev/doc/xxhash_spec.md
    // https://betterexplained.com/articles/understanding-big-and-little-endian-byte-order/

    static final long N0 = -7046029288634856825L;
    static final long N1 = -4417276706812531889L;
    static final long N2 = 1609587929392839161L;
    static final long N3 = -8796714831421723037L;
    static final long N4 = 2870177450012600261L;

    private final @NotNull Environment env;
    private final @NotNull Store store;
    private final @NotNull AtomicBoolean isClosed;

    public Bucket(@NotNull final Environment env, @NotNull final Store store) {
        this.env = env;
        this.store = store;
        this.isClosed = new AtomicBoolean();
    }

    static long hash(final long ihash, final long ih0, final long ih1) {
        long hash = ihash;
        long h0 = ih0;
        long h1 = ih1;
        h0 *= N1;
        h0 = (h0 << 31) | (h0 >>> -31);
        h0 *= N0;
        hash ^= h0;
        hash = hash * N0 + N3;
        h1 *= N1;
        h1 = (h1 << 31) | (h1 >>> -31);
        h1 *= N0;
        hash ^= h1;
        hash = hash * N0 + N3;
        return hash;
    }

    public static int getBucket(final @NotNull CharSequence id, final int bucketSize) {
        return CharSequenceHash.hash(id, bucketSize);
    }

    public static int getBucket(final long id, final int bucketSize) {
        return NumericHash.hash(id, bucketSize);
    }

    public boolean close() {
        return isClosed.compareAndSet(false, true);
    }

    public @NotNull Environment getEnv() {
        return env;
    }

    public @NotNull Store getStore() {
        return store;
    }
}
