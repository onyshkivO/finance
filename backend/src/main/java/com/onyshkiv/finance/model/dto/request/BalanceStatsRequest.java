package com.onyshkiv.finance.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceStatsRequest {

    @NotNull(message = "From date is required")
    @PastOrPresent(message = "From date must be in the past or present")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @NotNull(message = "To date is required")
    @PastOrPresent(message = "To date must be in the past or present")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    public boolean isDateRangeValid() {
        return ChronoUnit.DAYS.between(from, to) <= 90;
    }
}