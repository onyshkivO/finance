package com.onyshkiv.finance.model.dto.monobank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonobankCardResponse {
    private String id;
    private UUID cashboxId;
    private String type;
    private String currencyCode;
    private String maskedPan;
    private String iban;
    private Boolean isMonitoring;
}
