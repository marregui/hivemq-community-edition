package com.hivemq.tk.ds;

import com.hivemq.tk.*;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCharSequence implements CharSequence, CloneableMutable {

    public static String getString(CharSequence cs) {
        final Utf16Sink b = Files.getThreadLocalSink();
        b.put(cs);
        return b.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T copy() {
        return (T) getString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof CharSequence && Chars.equals(this, (CharSequence) obj);
    }

    @Override
    public int hashCode() {
        return Chars.hashCode(this);
    }

    @Override
    public final @NotNull CharSequence subSequence(int start, int end) {
        if (start == 0 && end == length()) {
            return this;
        }
        if (start == end) {
            return "";
        }
        return _subSequence(start, end);
    }

    @NotNull
    @Override
    public String toString() {
        return getString(this);
    }

    protected CharSequence _subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }
}
