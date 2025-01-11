package com.onyshkiv.finance.exception;

import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(int status, String message, Map<String, String> validationErrors) {
        super(status, message);
        this.validationErrors = validationErrors;
    }
}
