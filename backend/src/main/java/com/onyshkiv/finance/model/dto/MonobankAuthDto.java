package com.onyshkiv.finance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonobankAuthDto {
    private Boolean isConnected;
    private String tokenRequestId;
    private String acceptUrl;
}

