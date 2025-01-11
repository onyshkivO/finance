package com.onyshkiv.finance.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "UTC")
    private final Date timestamp;
    private int status;
    private String message;

    private ErrorResponse() {
        timestamp = new Date();
    }

    private ErrorResponse(int status) {
        this();
        this.status = status;
    }

    public ErrorResponse(int status, Throwable ex) {
        this(status);
        this.message = ex.getMessage();
    }

    public ErrorResponse(int status, String message) {
        this(status);
        this.message = message;
    }
}

