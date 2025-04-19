package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.response.*;
import com.onyshkiv.finance.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {
    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceStatsResponse> getBalanceStats(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        BalanceStatsResponse response = statsService.getBalanceStats(from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryStatsResponse>> getCategoryStats(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<CategoryStatsResponse> response = statsService.getCategoryStats(from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cashbox")
    public ResponseEntity<List<CashboxStatsResponse>> getCashboxStats(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<CashboxStatsResponse> response = statsService.getCashboxStats(from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/periods")
    public ResponseEntity<List<Integer>> getHistoryPeriods() {
        List<Integer> response = statsService.getTransactionHistoryPeriods();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/year")
    public ResponseEntity<List<YearlyTransactionSummary>> getYearlySummary(@RequestParam int year) {
        List<YearlyTransactionSummary> result = statsService.getYearlySummary(year);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/month")
    public ResponseEntity<List<MonthlyTransactionSummary>> getMonthlySummary(@RequestParam int year, @RequestParam int month) {
        List<MonthlyTransactionSummary> result = statsService.getMonthlySummary(year, month+1);
        return ResponseEntity.ok(result);
    }
}
