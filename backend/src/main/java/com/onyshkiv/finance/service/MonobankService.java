package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDto;

public interface MonobankService {
    void confirmAccess(String requestId);

    MonobankAuthDto requestAccessAndStore();

    void parseAndSaveTransactionWebhook(StatementItemDto statementItemDto);
}
