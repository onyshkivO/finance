package com.onyshkiv.finance.model.dto.monobank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StatementItemDto {
    private String account;
    private StatementItemDetailsDto statementItem;
}