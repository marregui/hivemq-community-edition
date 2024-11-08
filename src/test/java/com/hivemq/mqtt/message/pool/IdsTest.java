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

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class IdsTest {

    private static boolean areConsecutiveMessageIds(final @NotNull List<Integer> integerList) {
        int last = 0;
        for (int i = 0; i < Ids.MAX; i++) {
            final Integer integer = integerList.get(i);
            if (last + 1 != integer) {
                return false;
            }
            last = integer;
        }
        return true;
    }

    @Test
    public void lockId_whenTakingIdsSequentiallyAndReturning_thenSequentialIdsAreProvided()
            throws UnavailableIdException {
        final Ids ids = new Ids();
        final List<Integer> lockedIds = new ArrayList<>();
        for (int i = 0; i < Ids.MAX; i++) {
            lockedIds.add(ids.lockId());
        }
        for (final Integer id : lockedIds) {
            ids.unlockId(id);
        }
        for (int i = 0; i < Ids.MAX; i++) {
            lockedIds.add(ids.lockId());
        }

        assertTrue(areConsecutiveMessageIds(Lists.partition(lockedIds, Ids.MAX).get(0)));
        assertTrue(areConsecutiveMessageIds(Lists.partition(lockedIds, Ids.MAX).get(1)));
    }

    @Test
    public void testLockIdInSequence() throws UnavailableIdException {
        final Ids ids = new Ids();
        final int firstId = ids.lockId();
        assertEquals(1, firstId);
        final int secondId = ids.lockId();
        assertEquals(2, secondId);
        ids.unlockId(firstId);
        ids.lockId(firstId);
        assertEquals(3, ids.lockId());
    }

    @Test(expected = UnavailableIdException.class)
    public void testLockIdUnavailable() throws UnavailableIdException {
        final Ids ids = new Ids();
        for (int i = 0; i < Ids.MAX; i++) {
            ids.lockId();
        }
        ids.lockId();
    }

    @Test
    public void testUnlockId() throws UnavailableIdException {
        final Ids ids = new Ids();
        for (int i = 0; i < Ids.MAX; i++) {
            ids.lockId();
        }
        ids.unlockId(33333);
        assertEquals(33333, ids.lockId());
    }

    @Test
    public void testLockId() throws UnavailableIdException {
        final Ids ids = new Ids();
        ids.lockId(1978);
        try {
            ids.lockId(1978);
        } catch (final UnavailableIdException ignore) {
            //expected
        }
        for (int i = 0; i < Ids.MAX - 1; i++) {
            assertNotEquals(1978, ids.lockId());//since was taken directly
        }

        try {
            ids.lockId();
            Assert.fail("should not happen");
        } catch (final UnavailableIdException ignore) {
            //expected
        }

        ids.unlockId(1978);
        ids.lockId(1978);
        ids.unlockId(1978);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLockIdBeyondRange0() throws UnavailableIdException {
        final Ids ids = new Ids();
        ids.lockId(Ids.MAX + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnlockIdBeyondRange0() {
        final Ids ids = new Ids();
        ids.unlockId(Ids.MAX + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLockIdBeyondRange1() throws UnavailableIdException {
        final Ids ids = new Ids();
        ids.lockId(Ids.MIN - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnlockIdBeyondRange1() {
        final Ids ids = new Ids();
        ids.unlockId(Ids.MIN - 1);
    }

    @Test(expected = UnavailableIdException.class)
    public void testLockIdSameIdTwice() throws UnavailableIdException {
        final Ids ids = new Ids();
        ids.lockId(17); // <-- will fail
        ids.lockId(17); // <-- will fail
    }
}
