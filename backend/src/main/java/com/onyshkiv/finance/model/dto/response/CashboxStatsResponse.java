package com.onyshkiv.finance.model.dto.response;

import com.onyshkiv.finance.model.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CashboxStatsResponse {
    private TransactionType type;
    private String cashbox;
    private Double amount;
}

