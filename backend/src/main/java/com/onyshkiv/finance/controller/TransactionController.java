package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.util.ValidEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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


}
