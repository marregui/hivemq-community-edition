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

    static final long HASH0 = -7046029288634856825L;
    static final long HASH1 = -4417276706812531889L;
    static final long HASH2 = 1609587929392839161L;
    static final long HASH3 = -8796714831421723037L;
    static final long HASH4 = 2870177450012600261L;

    static long hash(final long ihash, final long ih0, final long ih1) {
        long hash = ihash;
        long h0 = ih0;
        long h1 = ih1;
        h0 *= HASH1;
        h0 = (h0 << 31) | (h0 >>> -31);
        h0 *= HASH0;
        hash ^= h0;
        hash = hash * HASH0 + HASH3;
        h1 *= HASH1;
        h1 = (h1 << 31) | (h1 >>> -31);
        h1 *= HASH0;
        hash ^= h1;
        hash = hash * HASH0 + HASH3;
        return hash;
    }

    static int idx(final long idx) {
        return (int) (idx >> 1);
    }

    private final @NotNull Environment environment;
    private final @NotNull Store store;
    private final @NotNull AtomicBoolean closing = new AtomicBoolean();

    public Bucket(@NotNull final Environment environment, @NotNull final Store store) {
        this.environment = environment;
        this.store = store;
    }

    public boolean close() {
        return closing.compareAndSet(false, true);
    }

    public @NotNull Environment getEnvironment() {
        return environment;
    }

    public @NotNull Store getStore() {
        return store;
    }
}
