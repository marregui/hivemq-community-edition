package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * An immutable on-heap sequence of UTF-8 bytes.
 */
public class Utf8String implements NativeChunk {
    public static final Utf8String EMPTY = new Utf8String("");
    private final boolean ascii;
    private final AsciiCharSequence asciiCharSequence = new AsciiCharSequence();
    private final byte[] bytes;

    public Utf8String(byte @NotNull [] bytes, boolean ascii) {
        this.bytes = bytes;
        this.ascii = ascii;
    }

    public Utf8String(@NotNull String str) {
        this.bytes = str.getBytes(StandardCharsets.UTF_8);
        this.ascii = (str.length() == bytes.length);
    }

    public Utf8String(char ch) {
        this.bytes = String.valueOf(ch).getBytes(StandardCharsets.UTF_8);
        this.ascii = (bytes.length == 1);
    }

    public Utf8String(@NotNull CharSequence seq) {
        this.bytes = seq.toString().getBytes(StandardCharsets.UTF_8);
        this.ascii = (seq.length() == bytes.length);
    }

    public static Utf8String newInstance(@NotNull NativeChunk src) {
        byte[] bytes = new byte[src.size()];
        for (int i = 0, n = src.size(); i < n; i++) {
            bytes[i] = src.byteAt(i);
        }
        return new Utf8String(bytes, src.isAscii());
    }

    @Override
    public @NotNull CharSequence asAsciiCharSequence() {
        return asciiCharSequence.of(this);
    }

    @Override
    public byte byteAt(int index) {
        return bytes[index];
    }

    public int intAt(int index) {
        return Unsafe.byteArrayGetInt(bytes, index);
    }

    @Override
    public boolean isAscii() {
        return ascii;
    }

    @Override
    public int size() {
        return bytes.length;
    }

    @Override
    public @NotNull String toString() {
        return Utf8s.stringFromUtf8Bytes(this);
    }
}
