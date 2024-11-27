

package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

/**
 * Builder class that allows JNI layer access CharSequence without copying memory. It is typically used to create file
 * system paths for files and directories and passing them to {@link Files} static methods, those that accept @link
 * {@link NativeChunk} as input.
 * <p>
 * Instances of this class can be re-cycled for creating many different paths and must be closed when no longer
 * required.
 */
public class Path implements Utf8Sink, NativeChunk, Closeable {

    private static final byte NULL = (byte) 0;
    private static final int OVERHEAD = 4;
    private static final boolean PARANOIA_MODE = false;
    public static final ThreadLocal<Path> PATH = new ThreadLocal<>(Path::new);
    public static final ThreadLocal<Path> PATH2 = new ThreadLocal<>(Path::new);
    public static final Closeable THREAD_LOCAL_CLEANER = Path::clearThreadLocals;
    private static final ThreadLocal<StringSink> tlSink = new ThreadLocal<>(StringSink::new);
    private final AsciiCharSequence asciiCharSequence = new AsciiCharSequence();
    private boolean ascii;
    private int capacity;
    private long headPtr;
    private long tailPtr;
    private final NativeChunk directUtf8Sequence = new NativeChunk() {
        @Override
        public @NotNull CharSequence asAsciiCharSequence() {
            return Path.this.asAsciiCharSequence();
        }

        @Override
        public long ptr() {
            return headPtr;
        }

        @Override
        public int size() {
            return (int) (tailPtr - headPtr);
        }
    };

    public Path() {
        this(255);
    }

    public Path(int capacity) {
        assert capacity > 0;
        this.capacity = capacity;
        headPtr = tailPtr = Unsafe.malloc(capacity + 1);
        if (PARANOIA_MODE) {
            randomSeed();
        }
        ascii = true;
    }

    public static void clearThreadLocals() {
        // It could be PATH.get.close(); but this would generated JDK failures on MacOS (SIGABRT)
        // when running tests. Despite all the effort to find the exact cause, it was not possible
        // and this is the best solution so far. This approach will remove the thread local
        // on close and the next time a new object is created.
        PATH.close();
        PATH2.close();
    }

    public static Path getThreadLocal(CharSequence root) {
        return PATH.get().of(root);
    }

    public static Path getThreadLocal(NativeChunk root) {
        return PATH.get().of(root);
    }

    /**
     * Creates path from another instance of Path. The assumption is that the source path is already UTF8 encoded and
     * does not require re-encoding.
     *
     * @param root path
     * @return copy of root path
     */
    public static Path getThreadLocal(Path root) {
        return PATH.get().of(root);
    }

    public static Path getThreadLocal2(Path root) {
        return PATH2.get().of(root);
    }

    public static Path getThreadLocal2(CharSequence root) {
        return PATH2.get().of(root);
    }

    public static int checkedLoHiSize(long lo, long hi, int baseSize) {
        final long additional = hi - lo;
        if (additional < 0) {
            throw new IllegalArgumentException("lo > hi");
        }
        final long size = baseSize + additional;

        if (size > (long) Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size exceeds 2GiB limit");
        }
        return (int) additional;
    }

    public NativeChunk $() {
        if (tailPtr == headPtr || Unsafe.UNSAFE.getByte(tailPtr) != NULL) {
            Unsafe.UNSAFE.putByte(tailPtr, NULL);
        }
        return directUtf8Sequence;
    }

    public void $at(int index) {
        Unsafe.UNSAFE.putByte(headPtr + index, NULL);
    }

    public @NotNull CharSequence asAsciiCharSequence() {
        return asciiCharSequence.of(this);
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public void close() {
        if (headPtr != 0L) {
            Unsafe.free(headPtr);
            headPtr = tailPtr = 0L;
        }
    }

    public Path concat(CharSequence str) {
        return concat(str, 0, str.length());
    }

    public Path concat(NativeChunk str) {
        ensureSeparator();
        return put(str);
    }

    public Path concat(long pUtf8NameZ) {
        ascii = false;
        ensureSeparator();
        long p = pUtf8NameZ;
        while (true) {
            byte b = Unsafe.UNSAFE.getByte(p++);
            if (b == NULL) {
                break;
            }

            int requiredCapacity = size();
            if (requiredCapacity + OVERHEAD >= capacity) {
                extend(requiredCapacity * 2 + OVERHEAD);
            }
            Unsafe.UNSAFE.putByte(tailPtr++, b);
        }
        return this;
    }

    public Path concat(CharSequence str, int from, int to) {
        ensureSeparator();
        return put(str, from, to);
    }

    public void extend(int newCapacity) {
        assert newCapacity > capacity;
        int size = size();
        headPtr = Unsafe.realloc(headPtr, capacity + 1, newCapacity + 1);
        tailPtr = headPtr + size;
        capacity = newCapacity;
    }

    public void flush() {
        $();
    }

    @Override
    public boolean isAscii() {
        return ascii;
    }

    public Path of(CharSequence str) {
        ascii = true;
        checkClosed();
        tailPtr = headPtr;
        return concat(str);
    }

    public Path of(NativeChunk str) {
        ascii = str.isAscii();
        checkClosed();
        if (str == this) {
            tailPtr = headPtr + str.size();
            return this;
        } else {
            tailPtr = headPtr;
            return concat(str);
        }
    }

    public Path of(Path other) {
        ascii = other.isAscii();
        return of((NativeChunk) other);
    }

    public Path of(NativeChunk other, boolean isAscii) {
        this.ascii = isAscii;
        // This is different from of(CharSequence str) because
        // another Path is already UTF8 encoded and cannot be treated as CharSequence.
        // Copy binary array representation instead of trying to UTF8 encode it
        int len = other.size();
        if (headPtr == 0L) {
            headPtr = Unsafe.malloc(len + 1);
            capacity = len;
        } else if (capacity < len) {
            extend(len);
        }

        if (len > 0) {
            Unsafe.UNSAFE.copyMemory(other.ptr(), headPtr, len);
        }
        tailPtr = headPtr + len;
        return this;
    }

    public Path of(CharSequence str, int from, int to) {
        ascii = true;
        checkClosed();
        tailPtr = headPtr;
        return concat(str, from, to);
    }

    public Path parent() {
        if (tailPtr > headPtr) {
            long p = tailPtr - 1;
            byte last = Unsafe.UNSAFE.getByte(p);
            if (last == Files.SLASH || last == NULL) {
                if (p < headPtr + 2) {
                    return this;
                }
                p--;
            }
            while (p > headPtr && Unsafe.UNSAFE.getByte(p) != Files.SLASH) {
                p--;
            }
            tailPtr = p;
        }
        return this;
    }

    @Override
    public long ptr() {
        return headPtr;
    }

    public void put(int index, byte b) {
        ascii = false;
        Unsafe.UNSAFE.putByte(headPtr + index, b);
    }

    @Override
    public Path put(@Nullable NativeChunk us) {
        if (us != null) {
            ascii &= us.isAscii();
            int size = us.size();
            checkExtend(size + 1);
            Utf8s.strCpy(us, size, tailPtr);
            tailPtr += size;
        }
        return this;
    }

    @Override
    public Path put(byte b) {
        ascii = false;
        return putByte0(b);
    }

    @Override
    public Path put(int value) {
        Utf8Sink.super.put(value);
        return this;
    }

    @Override
    public Path put(long value) {
        Utf8Sink.super.put(value);
        return this;
    }

    @Override
    public Path put(@NotNull CharSequence cs, int lo, int hi) {
        checkExtend(hi - lo + 1);
        Utf8Sink.super.put(cs, lo, hi);
        return this;
    }

    @Override
    public Path put(@Nullable CharSequence cs) {
        Utf8Sink.super.put(cs);
        return this;
    }

    @Override
    public Path put(char c) {
        Utf8Sink.super.put(c);
        return this;
    }

    @Override
    public Path putAscii(char @NotNull [] chars, int start, int len) {
        checkExtend(len + 1);
        Utf8Sink.super.putAscii(chars, start, len);
        return this;
    }

    @Override
    public Path putAscii(@NotNull CharSequence cs, int start, int len) {
        checkExtend(len + 1);
        Utf8Sink.super.putAscii(cs, start, len);
        return this;
    }

    @Override
    public Path putAscii(@Nullable CharSequence cs) {
        if (cs != null) {
            checkExtend(cs.length() + 1);
            Utf8Sink.super.putAscii(cs);
        }
        return this;
    }

    @Override
    public Path putAscii(char c) {
        return putByte0((byte) c);
    }

    @Override
    public Path putNonAscii(long lo, long hi) {
        ascii = false;
        final int size = checkedLoHiSize(lo, hi, this.size());
        checkExtend(size);
        Files.memcpy(tailPtr, lo, size);
        tailPtr += size;
        return this;
    }

    public Path seekZ() {
        int count = 0;
        while (count < capacity) {
            if (Unsafe.UNSAFE.getByte(headPtr + count) == NULL) {
                tailPtr = headPtr + count;
                break;
            }
            count++;
        }
        return this;
    }

    @Override
    public final int size() {
        return (int) (tailPtr - headPtr);
    }

    public Path slash() {
        ensureSeparator();
        return this;
    }

    public NativeChunk slash$() {
        ensureSeparator();
        return $();
    }

    public void toSink(Utf16Sink sink) {
        Utf8s.utf8ToUtf16(headPtr, tailPtr, sink);
    }

    @Override
    @NotNull
    public String toString() {
        if (headPtr != 0L) {
            // Don't use Misc.getThreadLocalBuilder() to convert Path to String.
            // This leads difficulties in debugging / running tests when FilesFacade tracks open files
            // when this method called implicitly
            final StringSink b = tlSink.get();
            b.clear();
            toSink(b);
            return b.toString();
        }
        return "";
    }

    public Path trimTo(int len) {
        tailPtr = headPtr + len;
        return this;
    }

    private void checkClosed() {
        if (headPtr == 0L) {
            headPtr = tailPtr = Unsafe.malloc(capacity + 1);
        }
    }

    private void checkExtend(int extra) {
        int requiredCapacity = size() + extra;
        if (requiredCapacity > capacity) {
            extend(requiredCapacity);
        }
    }

    @NotNull
    private Path putByte0(byte b) {
        int requiredCapacity = size() + 1;
        if (requiredCapacity >= capacity) {
            extend(requiredCapacity + 15);
        }
        Unsafe.UNSAFE.putByte(tailPtr++, b);
        return this;
    }

    private void randomSeed() {
        for (long p = headPtr, hi = headPtr + capacity + 1; p < hi; p++) {
            Unsafe.UNSAFE.putByte(p, (byte) (p % 127));
        }
    }

    protected final void ensureSeparator() {
        if (tailPtr > headPtr && Unsafe.UNSAFE.getByte(tailPtr - 1) != Files.SLASH) {
            putByte0((byte) Files.SLASH);
        }
    }

    static {
        Files.init();
    }

}
