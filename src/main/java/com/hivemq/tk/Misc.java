package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public final class Misc {
    public static final @NotNull String EOL = "\r\n";
    private static final @NotNull ThreadLocal<StringSink> tlSink = new ThreadLocal(StringSink::new);


    private Misc() {
    }

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
