

package com.hivemq.tk.time;

import com.hivemq.tk.NumericException;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDateFormat extends DateFormat {

    @Override
    public long parse(@NotNull CharSequence in, @NotNull DateLocale locale) throws NumericException {
        return parse(in, 0, in.length(), locale);
    }
}
