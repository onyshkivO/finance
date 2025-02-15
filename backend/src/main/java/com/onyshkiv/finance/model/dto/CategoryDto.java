package com.onyshkiv.finance.model.dto;

import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.util.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoryDto {
    private UUID id;

    @NotBlank(message = "Invalid category name")
    private String name;

    @ValidEnum(enumClass = TransactionType.class, message = "Invalid transaction type")
    private String type;

    Set<Integer> mccCodes;
}
