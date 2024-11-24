package com.hivemq.tk;

public class FatalError extends Error {
    public FatalError(Throwable cause) {
        super(cause);
    }

    public FatalError(String message) {
        super(message);
    }

    public FatalError(String message, Throwable cause) {
        super(message, cause);
    }
}
