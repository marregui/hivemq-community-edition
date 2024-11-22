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
package com.hivemq.topics;

import static com.hivemq.util.Bytes.set;

public final class SubscriptionFlag {
    public static final int SHARED = 1;
    public static final int RETAIN = 2;
    public static final int NON_LOCAL = 3;

    public static byte buildFlag(final boolean shared, final boolean retain, final boolean nonLocal) {
        return set(set(set((byte) 0, SHARED, shared), RETAIN, retain), NON_LOCAL, nonLocal);
    }
}
