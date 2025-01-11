package com.onyshkiv.finance.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {

    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
