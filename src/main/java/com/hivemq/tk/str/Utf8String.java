package com.hivemq.tk.str;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

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
