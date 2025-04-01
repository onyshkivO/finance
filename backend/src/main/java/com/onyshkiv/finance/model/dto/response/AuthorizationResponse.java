package com.onyshkiv.finance.model.dto.response;

import com.onyshkiv.finance.model.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationResponse {
    private String token;
    private String login;
    private UUID id;
    private Currency currency;
}
