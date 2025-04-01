package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.response.BalanceStatsResponse;
import com.onyshkiv.finance.model.dto.response.CategoryStatsResponse;
import com.onyshkiv.finance.model.dto.response.MonthlyTransactionSummary;
import com.onyshkiv.finance.model.dto.response.YearlyTransactionSummary;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {
    BalanceStatsResponse getBalanceStats(LocalDate from, LocalDate to);

    List<CategoryStatsResponse> getCategoryStats(LocalDate from, LocalDate to);

    List<Integer> getTransactionHistoryPeriods();

    List<YearlyTransactionSummary> getYearlySummary(int year);

    List<MonthlyTransactionSummary> getMonthlySummary(int year, int month);
}
