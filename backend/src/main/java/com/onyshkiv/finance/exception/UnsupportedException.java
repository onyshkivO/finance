package com.onyshkiv.finance.exception;

public class UnsupportedException extends RuntimeException {
    public UnsupportedException(String message) {
        super(message);
    }

    public UnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
