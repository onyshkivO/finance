package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {
    BalanceStatsResponse getBalanceStats(LocalDate from, LocalDate to);

    List<CategoryStatsResponse> getCategoryStats(LocalDate from, LocalDate to);

    List<Integer> getTransactionHistoryPeriods();

    List<YearlyTransactionSummary> getYearlySummary(int year);

    List<MonthlyTransactionSummary> getMonthlySummary(int year, int month);

    List<CashboxStatsResponse> getCashboxStats(LocalDate from, LocalDate to);
}
