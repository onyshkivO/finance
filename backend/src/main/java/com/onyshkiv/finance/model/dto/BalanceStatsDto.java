package com.onyshkiv.finance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceStatsDto {
    private double income = 0.0;
    private double expense = 0.0;
}