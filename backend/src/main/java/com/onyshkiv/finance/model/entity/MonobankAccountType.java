package com.onyshkiv.finance.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MonobankAccountType {
    BLACK("black"),
    WHITE("white"),
    PLATINUM("platinum"),
    IRON("iron"),
    FOP("fop"),
    YELLOW("yellow"),
    EAID("eAid");

    private String type;

    MonobankAccountType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static MonobankAccountType fromString(String key) {
        try {
            return key != null ? MonobankAccountType.valueOf(key.toUpperCase()) : null;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid monobank account type type: " + key);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

}
