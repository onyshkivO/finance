package com.onyshkiv.finance.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceStatsResponse {
    private Double expense;
    private Double income;
}