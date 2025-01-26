package com.onyshkiv.finance.model.dto.monobank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StatementItemDetailsDto {
    private String id;
    private LocalDateTime time;
    private String description;
    private Integer mcc;
    private Integer originalMcc;
    private BigDecimal amount;
    private BigDecimal operationAmount;
    private Integer currencyCode;
    private BigDecimal commissionRate;
    private BigDecimal cashbackAmount;
    private BigDecimal balance;
    private Boolean hold;
    private String receiptId;
}
