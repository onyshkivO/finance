package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.util.ValidEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto createTransaction(@RequestBody @Valid TransactionDto transactionDto) {
        return transactionService.save(transactionDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto updateTransaction(@PathVariable("id") UUID id, @RequestBody @Valid TransactionDto transactionDto) {
        return transactionService.updateTransaction(id, transactionDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable("id") UUID id) {
        transactionService.deleteTransaction(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto getTransactionById(@PathVariable("id") UUID id) {
        return transactionService.getTransactionById(id);
    }

    @GetMapping("/type/{transactionType}")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> getUserTransactions(@PathVariable @ValidEnum(enumClass = TransactionType.class) String transactionType) {
        return transactionService.getUserTransactions(TransactionType.valueOf(transactionType));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> getUserTransactionsByDateRange(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return transactionService.getUserTransactionsByDateRange(from, to);
    }
}
