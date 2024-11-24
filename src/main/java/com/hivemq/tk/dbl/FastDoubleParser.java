package com.hivemq.tk.dbl;

import com.hivemq.tk.NumericException;

public final class FastDoubleParser {

    private FastDoubleParser() {
    }

    public static double parseDouble(CharSequence str, boolean rejectOverflow) throws NumericException {
        return parseDouble(str, 0, str.length(), rejectOverflow);
    }

    public static double parseDouble(CharSequence str, int offset, int length, boolean rejectOverflow) throws NumericException {
        return FastDouble.parseFloatingPointLiteral(str, offset, length, rejectOverflow);
    }
}
