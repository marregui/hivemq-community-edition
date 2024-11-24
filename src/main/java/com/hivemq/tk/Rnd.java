package com.hivemq.tk;

public class Rnd {
    private long s0;
    private long s1;

    public Rnd() {
        reset();
    }

    public static void main(String[] args) {
        Rnd rnd = new Rnd();
        Utf8StringSink utf8sink = new Utf8StringSink();
        rnd.nextUtf8Str(512, utf8sink);

        StringSink utf16sink = new StringSink();
        if (!Utf8s.utf8ToUtf16(utf8sink, utf16sink)) {
            throw new RuntimeException();
        }
        System.out.println(utf16sink);
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(int boundary) {
        return nextPositiveInt() % boundary;
    }

    public long nextLong() {
        long l1 = s0;
        long l0 = s1;
        s0 = l0;
        l1 ^= l1 << 23;
        return (s1 = l1 ^ l0 ^ (l1 >> 17) ^ (l0 >> 26)) + l0;
    }

    public int nextPositiveInt() {
        int n = (int) nextLong();
        return n > 0 ? n : (n == Integer.MIN_VALUE ? Integer.MAX_VALUE : -n);
    }

    // https://stackoverflow.com/questions/1319022/really-good-bad-utf-8-example-test-data
    public void nextUtf8Str(int len, Utf8Sink sink) {
        for (int i = 0; i < len; i++) {
            // 5 is the exclusive upper limit for up to how many UTF8 bytes per character we generate
            int byteCount = Math.max(1, nextInt(5));
            switch (byteCount) {
                case 1:
                    sink.putAscii((char) (32 + nextPositiveInt() % (127 - 32)));
                    break;
                case 2:
                    while (true) {
                        // first byte of two-byte character, it has to start with 110xxxxx
                        final byte b1 = nextUtf8Byte(0xe0, 0xc0);
                        final byte b2 = nextUtf8ContinuationByte();

                        // rule out 0xc1 since 0xC0 and 0xC1 can't appear in valid UTF8 as the only characters
                        // that could be encoded by those are minimally encoded as single byte characters
                        if ((b1 & 30) == 0) {
                            continue;
                        }
                        sink.put(b1).put(b2);
                        break;
                    }
                    break;
                case 3:
                    while (true) {
                        // first byte of 3-byte character, it has to start with 1110xxxx
                        final byte b1 = nextUtf8Byte(0xf0, 0xe0);
                        final byte b2 = nextUtf8ContinuationByte();
                        final byte b3 = nextUtf8ContinuationByte();
                        final char c = Utf8s.utf8ToChar(b1, b2, b3);

                        // we might end up with surrogate, which we have to re-generate
                        if (Character.isSurrogate(c)) {
                            continue;
                        }
                        sink.put(b1).put(b2).put(b3);
                        break;
                    }
                    break;
                case 4:
                    // first byte of 4-byte character, it has to start with 11110xxx
                    while (true) {
                        final byte b1 = nextUtf8Byte(0xf8, 0xf0);
                        // remaining bytes start with continuation 10xxxxxx
                        final byte b2 = nextUtf8ContinuationByte();
                        final byte b3 = nextUtf8ContinuationByte();
                        final byte b4 = nextUtf8ContinuationByte();
                        if (Character.isSupplementaryCodePoint(Utf8s.getUtf8Codepoint(b1, b2, b3, b4))) {
                            sink.put(b1).put(b2).put(b3).put(b4);
                            break;
                        }
                    }
                    break;
                default:
                    assert false;
                    break;
            }
        }
    }

    public final void reset(long s0, long s1) {
        this.s0 = s0;
        this.s1 = s1;
    }

    public final void reset() {
        reset(0xdeadbeef, 0xdee4c0ed);
    }

    public void syncWith(Rnd other) {
        this.s0 = other.s0;
        this.s1 = other.s1;
    }

    private byte nextUtf8Byte(int wipe, int set) {
        while (true) {
            int k = nextInt();
            k &= ~wipe;
            k &= 0xff;
            if (k != 0) {
                k |= set;
                return (byte) k;
            }
        }
    }

    private byte nextUtf8ContinuationByte() {
        return nextUtf8Byte(0xc0, 0x80);
    }
}
