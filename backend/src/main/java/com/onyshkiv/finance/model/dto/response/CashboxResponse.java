package com.onyshkiv.finance.model.dto.response;

import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.util.ValidEnum;
import jakarta.validation.constraints.NotBlank;
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
