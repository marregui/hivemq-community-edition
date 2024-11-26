package com.hivemq.tk;

import com.hivemq.tk.ds.LongHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class Files {
    public static final int DT_FILE = 8;
    public static final char SLASH = File.separatorChar;
    private static final @NotNull AtomicInteger OPEN_FILE_COUNT = new AtomicInteger();
    private static final @NotNull AtomicInteger fdCounter = new AtomicInteger();
    private static final @NotNull LongHashSet openFds = new LongHashSet();

    static {
        final String os = System.getProperty("os.name");
        if (!(os.contains("Linux") || os.contains("Mac"))) {
            throw new Error("Unsupported: " + os);
        }
    }

    private Files() {
        // Prevent construction.
    }

    public static void memcpy(long dst, long src, long len) {
        // the split length was determined experimentally
        // using 'MemCopyBenchmark' bench
        if (len < 4096) {
            Unsafe.UNSAFE.copyMemory(src, dst, len);
        } else {
            memcpy0(src, dst, len);
        }
    }

    private static native void memcpy0(long src, long dst, long len);

    public static long append(long fd, long address, long len) {
        return append(toOsFd(fd), address, len);
    }

    public static int close(long fd) {
        // do not close `stdin` and `stdout`
        int osFd;
        if (fd > 0 && (osFd = toOsFd(fd)) > 2) {
            auditClose(fd);
            int res = close0(osFd);
            if (res == 0) {
                OPEN_FILE_COUNT.decrementAndGet();
            }
            return res;
        }
        // failed to close
        return -1;
    }

    public static int copy(NativeChunk from, NativeChunk to) {
        return copy(from.ptr(), to.ptr());
    }

    public static long createUniqueFd(int fd) {
        if (fd != -1) {
            long uniqueFd = auditOpen(fd);
            OPEN_FILE_COUNT.incrementAndGet();
            return uniqueFd;
        }
        return fd;
    }

    public static boolean exists(long fd) {
        return exists(toOsFd(fd));
    }

    public static boolean exists(NativeChunk NativeChunk) {
        return NativeChunk != null && exists0(NativeChunk.ptr());
    }

    public native static void findClose(long findPtr);

    public static long findFirst(NativeChunk NativeChunk) {
        return findFirst(NativeChunk.ptr());
    }

    public native static long findName(long findPtr);

    public native static int findNext(long findPtr);

    public native static int findType(long findPtr);

    public static long getDirSize(Path path) {
        long pFind = findFirst(path.$().ptr());
        if (pFind > 0L) {
            int len = path.size();
            try {
                long totalSize = 0L;
                do {
                    long nameUtf8Ptr = findName(pFind);
                    path.trimTo(len).concat(nameUtf8Ptr).$();
                    if (findType(pFind) == Files.DT_FILE) {
                        totalSize += length(path.$());
                    } else if (notDots(nameUtf8Ptr)) {
                        totalSize += getDirSize(path);
                    }
                } while (findNext(pFind) > 0);
                return totalSize;
            } finally {
                findClose(pFind);
                path.trimTo(len);
            }
        }
        return 0L;
    }

    public static long getDiskFreeSpace(NativeChunk path) {
        if (path != null) {
            return getDiskSize(path.ptr());
        }
        // current directory
        return 0L;
    }

    /**
     * Returns fs.file-max kernel limit on Linux or 0 on other OSes.
     */
    public native static long getFileLimit();

    public static long getLastModified(NativeChunk NativeChunk) {
        return getLastModified(NativeChunk.ptr());
    }


    public static @NotNull String getResourcePath(@Nullable URL url) {
        assert url != null;
        String file = url.getFile();
        assert file != null;
        assert !file.isEmpty();
        return file;
    }

    public synchronized static long getStdOutFdInternal() {
        int stdoutFd = getStdOutFd();
        long uniqueFd = Numbers.encodeLowHighInts(0, stdoutFd);
        openFds.add(uniqueFd);
        return uniqueFd;
    }

    public static native int hardLink(long DirectUtf8SequenceSrc, long DirectUtf8SequenceHardLink);

    public static int hardLink(NativeChunk src, NativeChunk hardLink) {
        return hardLink(src.ptr(), hardLink.ptr());
    }

    public static boolean isDirOrSoftLinkDir(NativeChunk path) {
        return isDir(path.ptr());
    }

    public native static boolean isSoftLink(long DirectUtf8SequencePath);

    public static boolean isSoftLink(NativeChunk path) {
        return isSoftLink(path.ptr());
    }

    public static long length(NativeChunk NativeChunk) {
        return length0(NativeChunk.ptr());
    }

    public static long length(long fd) {
        return length(toOsFd(fd));
    }

    public static int lock(long fd) {
        return lock(toOsFd(fd));
    }

    public static int mkdir(NativeChunk path, int mode) {
        return mkdir(path.ptr(), mode);
    }

    public static int mkdirs(Path path, int mode) {
        for (int i = 0, n = path.size(); i < n; i++) {
            byte b = path.byteAt(i);
            if (b == Files.SLASH) {
                // do not attempt to create '/' on linux or 'C:\' on Windows
                if (i == 0) {
                    continue;
                }

                // replace separator we just found with \0
                // temporarily truncate path to the directory we need to create
                path.$at(i);
                NativeChunk NativeChunk = path.$();
                if (path.size() > 0 && !Files.exists(NativeChunk)) {
                    int r = Files.mkdir(NativeChunk, mode);
                    if (r != 0) {
                        path.put(i, (byte) Files.SLASH);
                        return r;
                    }
                }
                path.put(i, (byte) Files.SLASH);
            }
        }
        return 0;
    }

    private static boolean notDots(long pUtf8NameZ) {
        final byte b0 = Unsafe.UNSAFE.getByte(pUtf8NameZ);
        if (b0 != '.') {
            return true;
        }
        final byte b1 = Unsafe.UNSAFE.getByte(pUtf8NameZ + 1);
        return b1 != 0 && (b1 != '.' || Unsafe.UNSAFE.getByte(pUtf8NameZ + 2) != 0);
    }

    public static long openAppend(NativeChunk NativeChunk) {
        return createUniqueFd(openAppend(NativeChunk.ptr()));
    }

    public static long openCleanRW(NativeChunk NativeChunk, long size) {
        return createUniqueFd(openCleanRW(NativeChunk.ptr(), size));
    }

    public native static int openCleanRW(long DirectUtf8SequenceName, long size);

    public static long openRO(NativeChunk NativeChunk) {
        return createUniqueFd(openRO(NativeChunk.ptr()));
    }

    public static long openRW(NativeChunk NativeChunk) {
        return createUniqueFd(openRW(NativeChunk.ptr()));
    }

    public static long openRW(NativeChunk NativeChunk, long opts) {
        return createUniqueFd(openRWOpts(NativeChunk.ptr(), opts));
    }

    public static long read(long fd, long address, long len, long offset) {
        return read(toOsFd(fd), address, len, offset);
    }

    public static boolean remove(NativeChunk NativeChunk) {
        return remove(NativeChunk.ptr());
    }

    public static int rename(NativeChunk oldName, NativeChunk newName) {
        return rename(oldName.ptr(), newName.ptr());
    }

    public static native int softLink(long DirectUtf8SequenceSrc, long DirectUtf8SequenceSoftLink);

    public static int toOsFd(long fd) {
        int osFd = Numbers.decodeHighInt(fd);
        // 0 FD can be closed, but no other operation is allowed
        assert fd == -1 || osFd > 0;
        return osFd;
    }

    public static boolean touch(NativeChunk NativeChunk) {
        long fd = openRW(NativeChunk);
        boolean result = fd > 0;
        if (result) {
            close(fd);
        }
        return result;
    }

    public static boolean truncate(long fd, long size) {
        return truncate(toOsFd(fd), size);
    }

    public static void walk(Path path, FindVisitor func) {
        int len = path.size();
        long p = findFirst(path.$());
        if (p > 0) {
            try {
                do {
                    long name = findName(p);
                    if (notDots(name)) {
                        int type = findType(p);
                        path.trimTo(len);
                        if (type == Files.DT_FILE) {
                            func.onFind(name, type);
                        } else {
                            walk(path.concat(name), func);
                        }
                    }
                } while (findNext(p) > 0);
            } finally {
                findClose(p);
            }
        }
    }

    public static long write(long fd, long address, long len, long offset) {
        return write(toOsFd(fd), address, len, offset);
    }

    private native static long append(int fd, long address, long len);

    private static synchronized void auditClose(long fd) {
        if (openFds.remove(fd) == -1) {
            throw new IllegalStateException("fd " + fd + " is already closed!");
        }
    }

    private static synchronized long auditOpen(int fd) {
        if (fd < 0) {
            throw new IllegalStateException("Invalid fd " + fd);
        }
        int index = fdCounter.getAndIncrement();
        long uniqueFd = Numbers.encodeLowHighInts(index, fd);
        openFds.add(uniqueFd);
        return uniqueFd;
    }

    private static synchronized void checkFdOpen(long fd) {
        if (!openFds.contains(fd)) {
            throw new IllegalStateException("fd " + fd + " is not open!");
        }
    }

    private native static int close0(int fd);

    private static native int copy(long from, long to);

    private static native boolean exists(int fd);

    private static native boolean exists0(long NativeChunk);

    // caller must call findClose to free allocated struct
    private native static long findFirst(long DirectUtf8SequenceName);

    private static native int fsync(int fd);

    private static native long getDiskSize(long DirectUtf8SequencePath);

    private native static long getLastModified(long DirectUtf8SequenceName);

    private native static int getStdOutFd();

    private native static boolean isDir(long pUtf8PathZ);

    private native static long length(int fd);

    private native static long length0(long DirectUtf8SequenceName);

    private static native int lock(int fd);

    private native static int mkdir(long DirectUtf8SequencePath, int mode);

    private native static int openAppend(long DirectUtf8SequenceName);

    private native static int openRO(long DirectUtf8SequenceName);

    private native static int openRW(long DirectUtf8SequenceName);

    private native static int openRWOpts(long DirectUtf8SequenceName, long opts);

    private native static long read(int fd, long address, long len, long offset);

    private native static boolean remove(long NativeChunk);

    private static native int rename(long DirectUtf8SequenceOld, long DirectUtf8SequenceNew);

    private native static boolean rmdir(long NativeChunk);

    private native static boolean truncate(int fd, long size);

    private native static long write(int fd, long address, long len, long offset);
}
