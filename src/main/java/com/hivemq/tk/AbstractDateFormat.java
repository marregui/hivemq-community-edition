package com.hivemq.tk;

import com.hivemq.tk.time.DateFormat;
import com.hivemq.tk.time.DateLocale;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDateFormat implements DateFormat {

    @Override
    public long parse(@NotNull CharSequence in, @NotNull DateLocale locale) throws NumericException {
        return parse(in, 0, in.length(), locale);
    }
}
