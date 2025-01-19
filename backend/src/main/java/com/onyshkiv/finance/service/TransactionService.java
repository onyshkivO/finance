package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionDto save(TransactionDto transactionDto);

    TransactionDto updateTransaction(UUID id, TransactionDto transactionDto);

    void deleteTransaction(UUID id);

    TransactionDto getTransactionById(UUID id);

    Transaction getTransaction(UUID id);

    List<TransactionDto> getTransactions();

    List<TransactionDto> getUserTransactions(TransactionType transactionType);

    void moveTransactionsToAnotherCategory(UUID sourceCategoryId, UUID targetCategoryId);
}
