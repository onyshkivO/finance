package com.onyshkiv.finance.model.dto.monobank;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StatementItemDetailsDto {
    private String id;
    private Long time;
    private String description;
    private Integer mcc;
    private Integer originalMcc;
    private BigInteger amount;
    private BigInteger operationAmount;
    private Integer currencyCode;
    private BigDecimal commissionRate;
    private BigDecimal cashbackAmount;
    private BigDecimal balance;
    private Boolean hold;
    private String receiptId;

    public LocalDate getTransactionDate() {
        return Instant.ofEpochSecond(time)
                .atZone(ZoneId.of("Europe/Kyiv"))
                .toLocalDateTime().toLocalDate();
    }
}
