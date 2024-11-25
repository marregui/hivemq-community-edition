package com.hivemq.tk.seq;

import com.hivemq.tk.DirectObjectFactory;
import com.hivemq.tk.Misc;
import com.hivemq.tk.Unsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

public class RingQueue<T> implements Closeable {
    private final @Nullable T@NotNull [] buf;
    private final int mask;
    private long memory;
    private long memorySize;


    @SuppressWarnings("unchecked")
    public RingQueue(final @NotNull DirectObjectFactory<T> factory, final long slotSize, final int cycle) {
        try {
            this.mask = cycle - 1;
            this.buf = (T[]) new Object[cycle];
            this.memorySize = slotSize * cycle;
            this.memory = Unsafe.calloc(memorySize);
            long p = memory;
            for (int i = 0; i < cycle; i++) {
                // intention is that whatever comes out of the factory it should work with the
                // memory allocated by the queue for this slot and should not reallocate ever
                buf[i] = factory.newInstance(p, slotSize);
                p += slotSize;
            }
        } catch (final @NotNull Throwable th) {
            close();
            throw th;
        }
    }

    @Override
    public void close() {
        for (int i = 0, n = buf.length; i < n; i++) {
            buf[i] = Misc.freeIfCloseable(buf[i]);
        }
        if (memory != 0) {
            memory = Unsafe.free(memory);
            this.memorySize = 0;
        }
    }

    public @Nullable T get(final long cursor) {
        return buf[(int) (cursor & mask)];
    }
}
