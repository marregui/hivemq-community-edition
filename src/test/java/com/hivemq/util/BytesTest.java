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
package com.hivemq.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BytesTest {


    @Test(expected = IllegalArgumentException.class)
    public void test_is_bit_setinvalid_argument_too_high() throws Exception {
        Bytes.isSet((byte) 0, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_is_bit_setinvalid_argument_too_small() throws Exception {
        Bytes.isSet((byte) 0, -1);
    }

    @Test
    public void test_no_bit_set() throws Exception {

        final byte b = 0b0000_0000;

        for (int i = 0; i < 8; i++) {
            assertFalse(Bytes.isSet(b, i));
        }
    }

    @Test
    public void test_all_bits_set() throws Exception {

        final byte b = (byte) 0b1111_1111;

        for (int i = 0; i < 8; i++) {
            assertTrue(Bytes.isSet(b, i));
        }
    }

    @Test
    public void test_bit_1_set() throws Exception {

        final byte b = (byte) 0b0000_0001;

        assertTrue(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_2_set() throws Exception {

        final byte b = (byte) 0b0000_0010;

        assertFalse(Bytes.isSet(b, 0));
        assertTrue(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_3_set() throws Exception {

        final byte b = (byte) 0b0000_0100;

        assertFalse(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertTrue(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_4_set() throws Exception {

        final byte b = (byte) 0b0000_1000;

        assertFalse(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertTrue(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_5_set() throws Exception {

        final byte b = (byte) 0b0001_0000;

        assertFalse(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertTrue(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_6_set() throws Exception {

        final byte b = (byte) 0b0010_0000;

        assertFalse(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertTrue(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_7_set() throws Exception {

        final byte b = (byte) 0b0100_0000;

        assertFalse(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertTrue(Bytes.isSet(b, 6));
        assertFalse(Bytes.isSet(b, 7));
    }

    @Test
    public void test_bit_8_set() throws Exception {

        final byte b = (byte) 0b1000_0000;

        assertFalse(Bytes.isSet(b, 0));
        assertFalse(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertFalse(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertFalse(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertTrue(Bytes.isSet(b, 7));
    }

    @Test
    public void test_some_bits_set() throws Exception {

        final byte b = (byte) 0b1010_1010;

        assertFalse(Bytes.isSet(b, 0));
        assertTrue(Bytes.isSet(b, 1));
        assertFalse(Bytes.isSet(b, 2));
        assertTrue(Bytes.isSet(b, 3));
        assertFalse(Bytes.isSet(b, 4));
        assertTrue(Bytes.isSet(b, 5));
        assertFalse(Bytes.isSet(b, 6));
        assertTrue(Bytes.isSet(b, 7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_long_buffer_overflow() throws Exception {
        Bytes.readLong(new byte[10], 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_int_buffer_overflow() throws Exception {
        Bytes.readLong(new byte[6], 3);
    }

    @Test
    public void test_copy_int_to_array() {
        final byte[] bytes = new byte[4];
        Bytes.copyIntToBytes(Integer.MAX_VALUE, bytes, 0);
        assertEquals(Integer.MAX_VALUE, Bytes.readInt(bytes, 0));

        Bytes.copyIntToBytes(Integer.MIN_VALUE, bytes, 0);
        assertEquals(Integer.MIN_VALUE, Bytes.readInt(bytes, 0));

        Bytes.copyIntToBytes(1, bytes, 0);
        assertEquals(1, Bytes.readInt(bytes, 0));

        Bytes.copyIntToBytes(-1, bytes, 0);
        assertEquals(-1, Bytes.readInt(bytes, 0));

        Bytes.copyIntToBytes(0, bytes, 0);
        assertEquals(0, Bytes.readInt(bytes, 0));

        final byte[] withOffset = new byte[7];
        Bytes.copyIntToBytes(Integer.MIN_VALUE, withOffset, 3);
        assertEquals(Integer.MIN_VALUE, Bytes.readInt(withOffset, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_copy_int_to_array_to_short() {
        final byte[] bytes = new byte[7];
        Bytes.copyIntToBytes(Integer.MAX_VALUE, bytes, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_copy_int_to_array_offset_negative() {
        final byte[] bytes = new byte[10];
        Bytes.copyIntToBytes(Integer.MAX_VALUE, bytes, -1);
    }

    @Test
    public void test_copy_long_to_byte_array() throws Exception {
        final byte[] bytes = new byte[8];

        Bytes.copyLongToBytes(Long.MAX_VALUE, bytes, 0);
        assertEquals(Long.MAX_VALUE, Bytes.readLong(bytes, 0));

        Bytes.copyLongToBytes(Long.MIN_VALUE, bytes, 0);
        assertEquals(Long.MIN_VALUE, Bytes.readLong(bytes, 0));

        Bytes.copyLongToBytes(0, bytes, 0);
        assertEquals(0, Bytes.readLong(bytes, 0));

        Bytes.copyLongToBytes(-1, bytes, 0);
        assertEquals(-1, Bytes.readLong(bytes, 0));

        Bytes.copyLongToBytes(1, bytes, 0);
        assertEquals(1, Bytes.readLong(bytes, 0));

        final byte[] withOffset = new byte[11];
        Bytes.copyLongToBytes(Long.MIN_VALUE, withOffset, 3);
        assertEquals(Long.MIN_VALUE, Bytes.readLong(withOffset, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_copy_long_to_array_to_short() {
        final byte[] bytes = new byte[11];
        Bytes.copyLongToBytes(Long.MAX_VALUE, bytes, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_copy_long_to_array_offset_negative() {
        final byte[] bytes = new byte[10];
        Bytes.copyLongToBytes(Long.MAX_VALUE, bytes, -1);
    }

    @Test(expected = NullPointerException.class)
    public void test_copy_long_to_array_null() {
        Bytes.copyLongToBytes(Long.MAX_VALUE, null, 0);
    }

    @Test
    public void test_copy_short_to_array() {
        final byte[] bytes = new byte[2];
        Bytes.copyUShortToBytes(65535, bytes, 0);
        assertEquals(65535, Bytes.readUShort(bytes, 0));

        Bytes.copyUShortToBytes(0, bytes, 0);
        assertEquals(0, Bytes.readUShort(bytes, 0));

        Bytes.copyUShortToBytes(1, bytes, 0);
        assertEquals(1, Bytes.readUShort(bytes, 0));

        Bytes.copyUShortToBytes(1, bytes, 0);
        assertEquals(1, Bytes.readUShort(bytes, 0));

        Bytes.copyUShortToBytes(0, bytes, 0);
        assertEquals(0, Bytes.readUShort(bytes, 0));

        final byte[] withOffset = new byte[7];
        Bytes.copyUShortToBytes(65535, withOffset, 5);
        assertEquals(65535, Bytes.readUShort(withOffset, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_copy_short_less_than_zero() {
        final byte[] bytes = new byte[2];
        Bytes.copyUShortToBytes(-1, bytes, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_copy_short_more_than_max() {
        final byte[] bytes = new byte[2];
        Bytes.copyUShortToBytes(65535 + 1, bytes, 0);
    }

    @Test(expected = NullPointerException.class)
    public void test_copy_short_null() {
        Bytes.copyUShortToBytes(65535, null, 0);
    }
}
