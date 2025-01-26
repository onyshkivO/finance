package com.onyshkiv.finance.model.dto.monobank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonobankClientDto {
    private String clientId;
    private String name;
    private String webHookUrl;
    private String permissions;
    private List<MonobankAccountDto> accounts;
}
