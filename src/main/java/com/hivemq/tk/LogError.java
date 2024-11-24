package com.hivemq.tk;

public class LogError extends Error {
    public LogError(String message) {
        super(message);
    }

    public LogError(String message, Throwable cause) {
        super(message, cause);
    }
}
