package com.hivemq.tk;

import com.hivemq.util.Bytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

public class Utf8StringSink implements MutableUtf8Sink {
    private final AsciiCharSequence asciiCharSequence = new AsciiCharSequence();
    private final int initialCapacity;
    private boolean ascii;
    private byte[] buffer;
    private int pos;

    public Utf8StringSink() {
        this(32);
    }

    public Utf8StringSink(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        this.buffer = new byte[initialCapacity];
        this.pos = 0;
        this.ascii = true;
    }

    @Override
    public @NotNull CharSequence asAsciiCharSequence() {
        return asciiCharSequence.of(this);
    }

    @Override
    public byte byteAt(int index) {
        return buffer[index];
    }

    @Override
    public void clear() {
        clear(0);
    }

    public void clear(int pos) {
        this.pos = pos;
        this.ascii = true;
    }

    @TestOnly
    public long getCapacity() {
        return buffer.length;
    }

    @Override
    public boolean isAscii() {
        return ascii;
    }

    @Override
    public long longAt(int offset) {
        return Unsafe.byteArrayGetLong(buffer, offset);
    }

    @Override
    public Utf8StringSink put(@Nullable Utf8Sequence us) {
        if (us != null) {
            ascii &= us.isAscii();
            int s = us.size();
            checkCapacity(s);
            for (int i = 0; i < s; i++) {
                buffer[pos + i] = us.byteAt(i);
            }
            pos += s;
        }
        return this;
    }

    @Override
    public Utf8StringSink put(byte b) {
        assert b < 0 : "b is ascii";
        ascii = false;
        return putByte0(b);
    }

    @Override
    public Utf8StringSink putAny(byte b) {
        ascii &= b >= 0;
        return putByte0(b);
    }

    @Override
    public Utf8StringSink putAscii(char c) {
        return putByte0((byte) c);
    }

    @Override
    public Utf8StringSink putNonAscii(long lo, long hi) {
        ascii = false;
        checkCapacity(Bytes.checkAddressingOverflow(lo, hi, pos));
        for (long p = lo; p < hi; p++) {
            buffer[pos++] = Unsafe.UNSAFE.getByte(p);
        }
        return this;
    }

    @Override
    public int size() {
        return pos;
    }

    @Override
    public @NotNull String toString() {
        return Utf8s.stringFromUtf8Bytes(this);
    }

    private void checkCapacity(int extra) {
        assert extra >= 0;
        int size = pos + extra;
        if (buffer.length > size) {
            return;
        }
        size = Math.max(pos * 2, size);
        final byte[] n = new byte[size];
        System.arraycopy(buffer, 0, n, 0, pos);
        buffer = n;
    }

    @NotNull
    private Utf8StringSink putByte0(byte b) {
        checkCapacity(1);
        buffer[pos++] = b;
        return this;
    }
}
