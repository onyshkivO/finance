package com.onyshkiv.finance.model.dto.response;

import com.onyshkiv.finance.model.dto.TransactionDto;
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
public class ExtendedCashboxResponse {
    private UUID id;

    private UUID userId;

    private String name;

    private String currency;

    private BigDecimal balance;

    private List<TransferResponse> transfers;

    private List<TransactionDto> transactions;
}
