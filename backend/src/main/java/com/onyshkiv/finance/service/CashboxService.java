package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.request.CashboxRequest;
import com.onyshkiv.finance.model.dto.request.TransferRequest;
import com.onyshkiv.finance.model.dto.response.CashboxResponse;
import com.onyshkiv.finance.model.dto.response.ExtendedCashboxResponse;
import com.onyshkiv.finance.model.dto.response.TransferResponse;
import com.onyshkiv.finance.model.entity.Cashbox;

import java.util.List;
import java.util.UUID;

public interface CashboxService {
    CashboxResponse save(Cashbox cashbox);
    Cashbox getCashbox(UUID id);

    CashboxResponse createCashCashbox(UUID userId);

    CashboxResponse createCashbox(CashboxRequest cashboxRequest);

    TransferResponse transfer(TransferRequest transferRequest);

    List<CashboxResponse> findCashboxes();

    ExtendedCashboxResponse findById(UUID id);

    void markDeleted(UUID id);

    CashboxResponse update(UUID id, CashboxRequest cashboxRequest);
}
