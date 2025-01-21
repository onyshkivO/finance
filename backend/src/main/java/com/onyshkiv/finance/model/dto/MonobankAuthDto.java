package com.onyshkiv.finance.model.dto;

import lombok.Data;

@Data
public class MonobankAuthDto {
    private String tokenRequestId;
    private String acceptUrl;
}

