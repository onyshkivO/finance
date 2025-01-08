package com.onyshkiv.finance.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(int code, String message, long timestamp, Map<String, String> validationErrors) {
        super(code, message, timestamp);
        this.validationErrors = validationErrors;
    }
}
