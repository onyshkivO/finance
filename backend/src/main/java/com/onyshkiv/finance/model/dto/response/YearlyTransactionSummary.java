package com.onyshkiv.finance.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YearlyTransactionSummary {
    private Double expense;
    private Double income;
    private Integer year;
    private Integer month;
}
