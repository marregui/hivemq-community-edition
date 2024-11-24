package com.hivemq.tk;

public class NumericException extends Exception {
    public static final NumericException INSTANCE = new NumericException();

    private NumericException() {
    }
}
