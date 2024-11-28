package com.hivemq.tk;

import com.hivemq.tk.ds.LongHashSet;
import com.hivemq.tk.str.NativeChunk;
import com.hivemq.tk.str.StringSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class Files {
    public static final char SLASH = File.separatorChar;
    public static final @NotNull String EOL = "\r\n";
    public static final int EOL_LENGTH = EOL.length();
    private static final long MICROS_IN_SECOND = 1_000_000;
    private static final @NotNull DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'").withZone(ZoneId.of("UTC"));
    private static final @NotNull AtomicInteger OPEN_FILE_COUNT = new AtomicInteger();
    private static final @NotNull AtomicInteger UNIQUE_FD = new AtomicInteger();
    private static final @NotNull LongHashSet openFds = new LongHashSet();
    private static final @NotNull AtomicBoolean inited = new AtomicBoolean();
    private static final @NotNull ThreadLocal<StringSink> tlSink = new ThreadLocal(StringSink::new);

    static {
        init();
    }

    public static @NotNull String microsToStr(final long micros) {
        return DT_FORMAT.format(Instant.ofEpochSecond(micros / MICROS_IN_SECOND, (micros % MICROS_IN_SECOND) * 1_000));
    }

    static void init() {
        if (inited.compareAndSet(false, true)) {
            final String os = System.getProperty("os.name");
            final String resource;
            if (os.contains("Linux")) {
                resource = "/files.so";
            } else if (os.contains("Mac")) {
                resource = "/libfiles.dylib";
            } else {
                inited.set(false);
                throw new Error("Unsupported OS: " + os);
            }
            final String path = Files.class.getResource(resource).getPath();
            System.out.printf("Loading %s... ", path);
            System.load(path);
            System.out.printf("Ok[%d]%n", getStdOutFd());
        }
    }

    public static long append(final long fd, final long address, final long len) {
        return append(osFd(fd), address, len);
    }

    public static int close(final long fd) {
        // do not close `stdin` and `stdout`
        final int osFd;
        if (fd > 0 && (osFd = osFd(fd)) > 2) {
            if (openFds.remove(fd) == -1) {
                throw new IllegalStateException("fd already closed: " + fd);
            }
            final int res = close0(osFd);
            if (res == 0) {
                OPEN_FILE_COUNT.decrementAndGet();
            }
            return res;
        }
        return -1;
    }

    public static long uniqueFd(final int fd) {
        if (fd != -1) {
            if (fd < 0) {
                throw new IllegalStateException("fd not valid: " + fd);
            }
            final int id = UNIQUE_FD.getAndIncrement();
            final long uniqueFd = Numbers.encodeLowHighInts(id, fd);
            openFds.add(uniqueFd);
            OPEN_FILE_COUNT.incrementAndGet();
            return uniqueFd;
        }
        return fd;
    }

    public static boolean exists(final long fd) {
        return exists(osFd(fd));
    }

    public static boolean exists(final @Nullable NativeChunk chunk) {
        return chunk != null && exists0(chunk.ptr());
    }

    public static @NotNull String getResourcePath(final @Nullable URL url) {
        assert url != null;
        final String file = url.getFile();
        assert file != null;
        assert !file.isEmpty();
        return file;
    }

    public synchronized static long getStdOutFdInternal() {
        final long uniqueFd = Numbers.encodeLowHighInts(0, getStdOutFd());
        openFds.add(uniqueFd);
        return uniqueFd;
    }

    public static long length(final @NotNull NativeChunk NativeChunk) {
        return length0(NativeChunk.ptr());
    }

    public static long length(final long fd) {
        return length(osFd(fd));
    }

    public static int lock(final long fd) {
        return lock(osFd(fd));
    }

    public static long openAppend(final @NotNull NativeChunk NativeChunk) {
        return uniqueFd(openAppend(NativeChunk.ptr()));
    }

    public static void memcpy(final long dst, final long src, final long len) {
        if (len < 4096) {
            Unsafe.UNSAFE.copyMemory(src, dst, len);
        } else {
            memcpy0(src, dst, len);
        }
    }

    public static long openRO(final @NotNull NativeChunk NativeChunk) {
        return uniqueFd(openRO(NativeChunk.ptr()));
    }

    public static long openRW(final @NotNull NativeChunk NativeChunk) {
        return uniqueFd(openRW(NativeChunk.ptr()));
    }

    public static long read(final long fd, final long address, final long len, final long offset) {
        return read(osFd(fd), address, len, offset);
    }

    public static boolean remove(final @NotNull NativeChunk NativeChunk) {
        return remove(NativeChunk.ptr());
    }

    private static int osFd(final long fd) {
        final int osFd = Numbers.decodeHighInt(fd);
        // 0 FD can be closed, but no other operation is allowed
        assert fd == -1 || osFd > 0;
        return osFd;
    }

    public static boolean truncate(final long fd, final long size) {
        return truncate(osFd(fd), size);
    }

    public static long write(final long fd, final long address, final long len, final long offset) {
        return write(osFd(fd), address, len, offset);
    }

    public static void pause() {
        try {
            Thread.sleep(0);
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleep(final long millis) {
        long t = System.currentTimeMillis();
        long deadline = millis;
        while (deadline > 0) {
            try {
                Thread.sleep(deadline);
                break;
            } catch (final @NotNull InterruptedException e) {
                final long t2 = System.currentTimeMillis();
                deadline -= t2 - t;
                t = t2;
            }
        }
    }

    public native static long append(int fd, long address, long len);

    public native static int close0(int fd);

    public static native boolean exists(int fd);

    public static native boolean exists0(long ptr);

    public native static int getStdOutFd();

    public native static long length(int fd);

    public native static long length0(long DirectUtf8SequenceName);

    public static native int lock(int fd);

    public static native void memcpy0(long src, long dst, long len);

    public native static int openAppend(long DirectUtf8SequenceName);

    public native static int openRO(long DirectUtf8SequenceName);

    public native static int openRW(long DirectUtf8SequenceName);

    public native static long read(int fd, long address, long len, long offset);

    public native static boolean remove(long NativeChunk);

    public native static boolean truncate(int fd, long size);

    public native static long write(int fd, long address, long len, long offset);

    public static native long currentTimeMicros();

    public static <T extends Closeable> @Nullable T free(final @Nullable T object) {
        if (object != null) {
            try {
                object.close();
            } catch (final IOException e) {
                throw new Error(e);
            }
        }
        return null;
    }

    // same as free() but can be used when input object type is not guaranteed to be Closeable
    public static <T> @Nullable T freeIfCloseable(final @Nullable T object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (final IOException e) {
                throw new Error(e);
            }
        }
        return null;
    }

    public static @NotNull StringSink getThreadLocalSink() {
        final StringSink b = tlSink.get();
        b.clear();
        return b;
    }
}
