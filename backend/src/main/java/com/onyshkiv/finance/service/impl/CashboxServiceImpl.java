package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.DuplicationException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.exception.UnsupportedException;
import com.onyshkiv.finance.model.dto.request.CashboxRequest;
import com.onyshkiv.finance.model.dto.request.TransferRequest;
import com.onyshkiv.finance.model.dto.response.CashboxResponse;
import com.onyshkiv.finance.model.dto.response.ExtendedCashboxResponse;
import com.onyshkiv.finance.model.dto.response.TransferResponse;
import com.onyshkiv.finance.model.entity.Cashbox;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.Transfer;
import com.onyshkiv.finance.repository.CashboxRepository;
import com.onyshkiv.finance.repository.TransferRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CashboxService;
import com.onyshkiv.finance.service.CurrencyService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CashboxServiceImpl implements CashboxService {
    private final CashboxRepository cashboxRepository;
    private final ApplicationMapper applicationMapper;
    private final SecurityContextHelper securityContextHelper;
    private final CurrencyService currencyService;
    private final TransferRepository transferRepository;

    @Autowired
    public CashboxServiceImpl(CashboxRepository cashboxRepository, ApplicationMapper applicationMapper, SecurityContextHelper securityContextHelper, CurrencyService currencyService, TransferRepository transferRepository) {
        this.cashboxRepository = cashboxRepository;
        this.applicationMapper = applicationMapper;
        this.securityContextHelper = securityContextHelper;
        this.currencyService = currencyService;
        this.transferRepository = transferRepository;
    }

    @Transactional
    @Override
    public CashboxResponse createCashCashbox(UUID userId) {
        Cashbox cashbox = Cashbox.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Cash")
                .currency(Currency.UAH)
                .balance(BigDecimal.ZERO)
                .build();
        return save(cashbox);
    }

    @Transactional
    @Override
    public CashboxResponse createCashbox(CashboxRequest cashboxRequest) {
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        Cashbox cashbox = applicationMapper.cashboxRequestToCashbox(cashboxRequest);
        cashbox.setUserId(userId);

        return save(cashbox);
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest transferRequest) {
        securityContextHelper.validateLoggedInUser();
        BigDecimal fromAmount = transferRequest.getAmount();
        Cashbox cashboxFrom = getCashbox(transferRequest.getCashboxFromId());
        Cashbox cashboxTo = getCashbox(transferRequest.getCashboxToId());
        if (cashboxFrom.getBalance().compareTo(fromAmount) < 0) {
            throw new UnsupportedException("Cashbox " + cashboxFrom.getName() + " balance is not enough!");
        }
        BigDecimal toAmount = transferAmount(fromAmount, cashboxFrom.getCurrency(), cashboxTo.getCurrency(), transferRequest.getDate());
        cashboxFrom.setBalance(cashboxFrom.getBalance().subtract(fromAmount));
        cashboxTo.setBalance(toAmount);
        Transfer transfer = Transfer.builder()
                .id(UUID.randomUUID())
                .fromCashbox(cashboxFrom)
                .toCashbox(cashboxTo)
                .description(transferRequest.getDescription())
                .date(transferRequest.getDate())
                .fromAmount(fromAmount)
                .toAmount(toAmount)
                .build();
        Transfer savedTransfer = transferRepository.save(transfer);
        log.info("transfer created successfully: {}", transfer);
        return applicationMapper.transferToTransferResponse(savedTransfer);
    }

    @Override
    public List<CashboxResponse> findCashboxes() {
        UUID loggedInUserId = securityContextHelper.getLoggedInUser().getId();
        List<Cashbox> userCashboxes = cashboxRepository.findAllUserCashboxes(loggedInUserId);
        return userCashboxes.stream().map(applicationMapper::cashboxToCashboxResponse).toList();
    }

    @Override
    public ExtendedCashboxResponse findById(UUID id) {
        Cashbox cashbox = getCashbox(id);
        return applicationMapper.cashboxToExtendedCashboxResponse(cashbox);
    }

    @Override
    @Transactional
    public void markDeleted(UUID id) {
        securityContextHelper.validateLoggedInUser();
        Cashbox cashbox = getCashbox(id);
        if (cashbox.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new UnsupportedException("Current cashbox have not 0 balance, please transfer balance to another cashbox to be able to delete cashbox");
        }
        cashbox.setDeletedAt(OffsetDateTime.now());
    }

    @Override
    public CashboxResponse update(UUID id, CashboxRequest cashboxRequest) {
        UUID loggedUserId = securityContextHelper.getLoggedInUser().getId();
        Cashbox cashbox = getCashbox(id);
        Optional<Cashbox> cashboxFromDb = cashboxRepository.findByUserIdAndName(loggedUserId, cashboxRequest.getName());
        if (cashboxFromDb.isPresent()) {
            log.error("Cashbox with name {} already exists", cashbox.getName());
            throw new DuplicationException("Cashbox with name " + cashbox.getName() + " already exists");
        }
        cashbox.setName(cashboxRequest.getName());
        return applicationMapper.cashboxToCashboxResponse(cashbox);
    }

    private BigDecimal transferAmount(BigDecimal amount, Currency currencyFrom, Currency currencyTo, LocalDate transferDate) {
        if (currencyFrom.equals(currencyFrom)) {
            return amount;
        } else {
            return currencyService.convert(
                    amount,
                    currencyFrom,
                    currencyTo,
                    transferDate);
        }
    }

    @Transactional
    @Override
    public CashboxResponse save(Cashbox cashbox) {
        Optional<Cashbox> cashboxFromDbOptional = cashboxRepository.findByUserIdAndName(cashbox.getUserId(), cashbox.getName());
        if (cashboxFromDbOptional.isPresent() && cashboxFromDbOptional.get().getDeletedAt() == null) {
            log.error("Cashbox with name {} already exists", cashbox.getName());
            throw new DuplicationException("Cashbox with name " + cashbox.getName() + " already exists");
        } else if (cashboxFromDbOptional.isPresent()) {
            cashboxFromDbOptional.get().setDeletedAt(null);
            log.info("CashboxServiceImpl save : cashbox successfully saved restored from deleted : {}", cashboxFromDbOptional.get());
            return applicationMapper.cashboxToCashboxResponse(cashboxFromDbOptional.get());
        } else {
            Cashbox savedCashbox = cashboxRepository.save(cashbox);
            log.info("CashboxServiceImpl save : cashbox successfully saved : {}", savedCashbox);
            return applicationMapper.cashboxToCashboxResponse(savedCashbox);
        }
    }

    @Override
    public Cashbox getCashbox(UUID id) {
        return cashboxRepository.findById(id).orElseThrow(() -> {
            log.error("CashboxService : Cashbox not found with id {}", id);
            return new NotFoundException(String.format("Cashbox not found with id %s", id));
        });
    }
}
