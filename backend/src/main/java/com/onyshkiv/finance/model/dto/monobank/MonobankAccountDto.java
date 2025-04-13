package com.onyshkiv.finance.model.dto.monobank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonobankAccountDto {
    private String id;
    private String sendId;
    private UUID cashboxId;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private String type;
    private Integer currencyCode;
    private String cashbackType;
    private List<String> maskedPan;
    private String iban;
}
