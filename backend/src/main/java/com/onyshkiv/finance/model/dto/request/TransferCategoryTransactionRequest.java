package com.onyshkiv.finance.model.dto.request;

import com.onyshkiv.finance.model.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferCategoryTransactionRequest {
    @NotNull
    private TransactionType type;
    private UUID categoryIdFrom;
    private UUID categoryIdTo;
}