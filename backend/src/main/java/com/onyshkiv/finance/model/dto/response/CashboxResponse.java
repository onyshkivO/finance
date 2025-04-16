package com.onyshkiv.finance.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CashboxResponse {
    private UUID id;

    private UUID userId;

    private String name;

    private String currency;

    private BigDecimal balance;
}
