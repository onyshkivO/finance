package com.onyshkiv.finance.model.dto.response;

import com.onyshkiv.finance.model.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryStatsResponse {
    private TransactionType type;
    private String category;
    private String icon;
    private Double amount;
}
