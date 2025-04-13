package com.onyshkiv.finance.model.dto.request;

import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.util.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CashboxRequest {
    @NotBlank(message = "Invalid cashbox name")
    private String name;

    @NotBlank(message = "Invalid cashbox name")
    @ValidEnum(enumClass = Currency.class, message = "Invalid currency")
    private String currency;
}

