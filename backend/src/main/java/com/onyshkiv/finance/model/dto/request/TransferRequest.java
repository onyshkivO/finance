package com.onyshkiv.finance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    @NotNull(message = "invalid transaction date")
    @Min(value = 0, message = "amount should be positive value")
    private BigDecimal amount;

    @NotNull(message = "invalid currency coefficient")
    @Min(value = 0, message = "currency should be positive value")
    private BigDecimal currencyCoefficient;

    private UUID cashboxFromId;

    private UUID cashboxToId;

    @NotNull(message = "invalid transaction date")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;

    private String description;
}
