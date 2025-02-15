package com.onyshkiv.finance.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.onyshkiv.finance.exception.UnsupportedException;
import com.onyshkiv.finance.exception.ValidationErrorResponse;

public enum TransactionType {
    INCOME,
    EXPENSE;

    @JsonCreator
    public static TransactionType fromString(String key) {
        try {
            return key != null ? TransactionType.valueOf(key.toUpperCase()) : null;
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException("Invalid transaction type: " + key);
        }
    }
    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
