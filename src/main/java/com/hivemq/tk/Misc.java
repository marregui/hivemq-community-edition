package com.hivemq.tk;

import com.hivemq.tk.ds.ObjList;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public final class Misc {
    public static final String EOL = "\r\n";
    private static final ThreadLocal<StringSink> tlSink = ThreadLocal.withInitial(() -> new StringSink());


    private Misc() {
    }

    public static <T extends Mutable> T clear(T object) {
        if (object != null) {
            object.clear();
        }
        return null;
    }

    public static <T extends Closeable> T free(T object) {
        if (object != null) {
            try {
                object.close();
            } catch (IOException e) {
                throw new FatalError(e);
            }
        }
        return null;
    }

    public static <T extends Closeable> void free(T[] list) {
        if (list != null) {
            for (int i = 0, n = list.length; i < n; i++) {
                list[i] = Misc.free(list[i]);
            }
        }
    }

    // same as free() but can be used when input object type is not guaranteed to be Closeable
    public static <T> T freeIfCloseable(T object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                throw new FatalError(e);
            }
        }
        return null;
    }

    public static <T extends Closeable> void freeObjListAndClear(ObjList<T> list) {
        if (list != null) {
            for (int i = 0, n = list.size(); i < n; i++) {
                free(list.getQuick(i));
            }
            list.clear();
        }
    }

    public static StringSink getThreadLocalSink() {
        StringSink b = tlSink.get();
        b.clear();
        return b;
    }

    public static int[] getWorkerAffinity(int workerCount) {
        int[] res = new int[workerCount];
        Arrays.fill(res, -1);
        return res;
    }
}
