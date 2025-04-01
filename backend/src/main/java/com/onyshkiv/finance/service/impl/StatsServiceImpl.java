package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.UnsupportedException;
import com.onyshkiv.finance.model.dto.response.BalanceStatsResponse;
import com.onyshkiv.finance.model.dto.response.CategoryStatsResponse;
import com.onyshkiv.finance.model.dto.response.MonthlyTransactionSummary;
import com.onyshkiv.finance.model.dto.response.YearlyTransactionSummary;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.TransactionRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.onyshkiv.finance.model.entity.TransactionType.EXPENSE;
import static com.onyshkiv.finance.model.entity.TransactionType.INCOME;

@Service
@Slf4j
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final TransactionRepository transactionRepository;
    private final SecurityContextHelper securityContextHelper;

    @Autowired
    public StatsServiceImpl(TransactionRepository transactionRepository, SecurityContextHelper securityContextHelper) {
        this.transactionRepository = transactionRepository;
        this.securityContextHelper = securityContextHelper;
    }

    @Override
    public BalanceStatsResponse getBalanceStats(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        List<Object[]> results = transactionRepository.getBalanceStats(userId, from, to);

        double income = 0;
        double expense = 0;

        for (Object[] row : results) {
            TransactionType type = (TransactionType) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            if (INCOME.equals(type)) {
                income = amount != null ? amount.doubleValue() : 0;
            } else if (EXPENSE.equals(type)) {
                expense = amount != null ? amount.doubleValue() : 0;
            }
        }

        return new BalanceStatsResponse(expense, income);
    }

    public List<CategoryStatsResponse> getCategoryStats(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        List<Object[]> results = transactionRepository.getCategoriesStats(userId, from, to);

        return results.stream()
                .map(result -> new CategoryStatsResponse(
                        (TransactionType) result[0],
                        (String) result[1],
                        (String) result[2],
                        ((BigDecimal) result[3]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getTransactionHistoryPeriods() {
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        List<Integer> years = transactionRepository.getTransactionHistoryPeriods(userId);
        if (years.isEmpty()) {
            years.add(Year.now().getValue());
        }
        return years;
    }

    public List<YearlyTransactionSummary> getYearlySummary(int year) {
        List<Object[]> results = transactionRepository.sumAmountByYearGroupedByMonthAndType(year);
        return mapYearlyResults(results, year);
    }

    public List<MonthlyTransactionSummary> getMonthlySummary(int year, int month) {
        List<Object[]> results = transactionRepository.sumAmountByMonthGroupedByDayAndType(year, month);
        return mapMonthlyResults(results, year, month);
    }

    private List<YearlyTransactionSummary> mapYearlyResults(List<Object[]> results, int year) {
        Map<Integer, BigDecimal> expenseMap = new HashMap<>();
        Map<Integer, BigDecimal> incomeMap = new HashMap<>();

        for (Object[] row : results) {
            Integer month = ((Number) row[0]).intValue();
            TransactionType type = (TransactionType) row[1];
            BigDecimal sum = (BigDecimal) row[2];

            if (type == TransactionType.EXPENSE) {
                expenseMap.put(month, sum);
            } else {
                incomeMap.put(month, sum);
            }
        }

        List<YearlyTransactionSummary> summaryList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            summaryList.add(new YearlyTransactionSummary(
                    expenseMap.getOrDefault(month, BigDecimal.ZERO).doubleValue(),
                    incomeMap.getOrDefault(month, BigDecimal.ZERO).doubleValue(),
                    year, month));
        }
        return summaryList;
    }

    private List<MonthlyTransactionSummary> mapMonthlyResults(List<Object[]> results, int year, int month) {
        Map<Integer, BigDecimal> expenseMap = new HashMap<>();
        Map<Integer, BigDecimal> incomeMap = new HashMap<>();

        for (Object[] row : results) {
            Integer day = ((Number) row[0]).intValue();
            TransactionType type = (TransactionType) row[1];
            BigDecimal sum = (BigDecimal) row[2];

            if (type == TransactionType.EXPENSE) {
                expenseMap.put(day, sum);
            } else {
                incomeMap.put(day, sum);
            }
        }

        List<MonthlyTransactionSummary> summaryList = new ArrayList<>();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            summaryList.add(new MonthlyTransactionSummary(
                    expenseMap.getOrDefault(day, BigDecimal.ZERO).doubleValue(),
                    incomeMap.getOrDefault(day, BigDecimal.ZERO).doubleValue(),
                    year, month, day));
        }
        return summaryList;
    }

    public void validateDateRange(LocalDate from, LocalDate to) {
        if (ChronoUnit.DAYS.between(from, to) > 90) {
            throw new UnsupportedException("Date range cannot exceed 90 days.");
        }
    }
}
