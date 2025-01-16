package com.onyshkiv.finance.model.dto;

import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.util.ValidEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TransactionDto {
    private UUID id;

    @NotNull(message = "Category cannot be null")
//    @Valid//todo we only need id if we create or update transaction, so validation for name may be wrong? maybe remove this type and only ad id as UUID value
    private CategoryDto category;

    @ValidEnum(enumClass = TransactionType.class, message = "Invalid transaction type")
    private String type;

    @NotNull(message = "invalid transaction date")
    @Min(value = 0, message = "amount should be positive value")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "invalid transaction date")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate transactionDate;
}
