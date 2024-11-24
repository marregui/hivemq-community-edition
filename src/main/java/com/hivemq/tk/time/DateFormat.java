package com.hivemq.tk.time;

import com.hivemq.tk.CharSink;
import com.hivemq.tk.NumericException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DateFormat {

    void format(
            long datetime,
            @NotNull DateLocale locale,
            @Nullable CharSequence timeZoneName,
            @NotNull CharSink<?> sink);

    long parse(@NotNull CharSequence in, @NotNull DateLocale locale) throws NumericException;

    long parse(@NotNull CharSequence in, int lo, int hi, @NotNull DateLocale locale) throws NumericException;
}
