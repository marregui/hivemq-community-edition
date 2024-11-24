package com.hivemq.tk.time;

import com.hivemq.tk.CharSink;
import com.hivemq.tk.NumericException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DateFormat {

    public long parse(final @NotNull CharSequence in, final @NotNull DateLocale locale) throws NumericException {
        return parse(in, 0, in.length(), locale);
    }

    protected abstract void format(
            long datetime,
            @NotNull DateLocale locale,
            @Nullable CharSequence timeZoneName,
            @NotNull CharSink<?> sink);

    protected abstract long parse(@NotNull CharSequence in, int lo, int hi, @NotNull DateLocale locale)
            throws NumericException;
}
