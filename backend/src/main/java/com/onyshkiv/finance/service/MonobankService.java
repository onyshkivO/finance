package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankAccountDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDto;

import java.util.List;

public interface MonobankService {
    void confirmAccess(String requestId);

    MonobankAuthDto requestAccessAndStore();

    void parseAndSaveTransactionWebhook(StatementItemDto statementItemDto);

    void monitorAccount(String accountId);
    void unmonitorAccount(String accountId);

    List<MonobankAccountDto> getUserMonobankAccounts();
}
