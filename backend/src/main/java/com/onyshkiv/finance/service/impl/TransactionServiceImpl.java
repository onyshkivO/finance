package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.exception.UnsupportedException;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.TransactionRepository;
import com.onyshkiv.finance.security.CustomUserDetails;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CategoryService;
import com.onyshkiv.finance.service.CurrencyService;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ApplicationMapper applicationMapper;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, SecurityContextHelper securityContextHelper, ApplicationMapper applicationMapper, CategoryService categoryService, CurrencyService currencyService) {
        this.transactionRepository = transactionRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
        this.categoryService = categoryService;
        this.currencyService = currencyService;
    }

    @Transactional
    @Override
    public TransactionDto save(TransactionDto transactionDto) {
        CustomUserDetails loggedInUser = securityContextHelper.getLoggedInUser();

        Transaction transaction = applicationMapper.transactionDtoToTransaction(transactionDto);
        if (!categoryService.validateCategoryType(transactionDto.getCategory().getId(), transaction.getType())) {
            throw new UnsupportedException(String.format("category type differ with type %s",
                    transaction.getType()));
        }
        transaction.setId(UUID.randomUUID());
        transaction.setCategory(categoryService.getCategory(transactionDto.getCategory().getId()));
        transaction.setUserId(loggedInUser.getId());

        setTransactionAmountInternal(transactionDto.getAmount(), transactionDto.getCurrency(), loggedInUser.getCurrency(), transaction);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("TransactionService save : transaction successfully saved : {}", savedTransaction);
        return applicationMapper.transactionToTransactionDto(transaction);
    }

    @Transactional
    @Override
    public TransactionDto updateTransaction(UUID id, TransactionDto transactionDto) {
        CustomUserDetails loggedInUser = securityContextHelper.getLoggedInUser();

        Transaction transaction = getTransaction(id);
        if (!categoryService.validateCategoryType(transactionDto.getCategory().getId(), transaction.getType())) {
            throw new UnsupportedException(String.format("category type differ with type %s",
                    transaction.getType()));
        }

        transaction.setTransactionDate(transactionDto.getTransactionDate());
        transaction.setCategory(categoryService.getCategory(transactionDto.getCategory().getId()));
        setTransactionAmountInternal(transactionDto.getAmount(), transactionDto.getCurrency(), loggedInUser.getCurrency(), transaction);

        transaction.setDescription(transactionDto.getDescription());

        return applicationMapper.transactionToTransactionDto(transaction);
    }

    @Transactional
    @Override
    public void deleteTransaction(UUID id) {
        securityContextHelper.validateLoggedInUser();
        transactionRepository.deleteById(id);
        log.info("TransactionService deleteTransaction : Transaction successfully deleted with id : {}", id);
    }

    @Override
    public TransactionDto getTransactionById(UUID id) {
        securityContextHelper.validateLoggedInUser();
        Transaction transaction = getTransaction(id);
        return applicationMapper.transactionToTransactionDto(transaction);
    }

    @Override
    public Transaction getTransaction(UUID id) {
        Optional<Transaction> transactionOptional = transactionRepository.findById(id);
        if (transactionOptional.isEmpty()) {
            log.error("TransactionService getTransaction : Transaction not found with id {} for user {}", id, securityContextHelper.getLoggedInUser().getLogin());
            throw new NotFoundException(String.format("Transaction not found with id %s for user %s", id, securityContextHelper.getLoggedInUser().getLogin()));
        }
        return transactionOptional.get();
    }

    @Override
    public List<TransactionDto> getTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(applicationMapper::transactionToTransactionDto)
                .toList();
    }

    @Override
    public List<TransactionDto> getUserTransactions(TransactionType transactionType) {
        securityContextHelper.validateLoggedInUser();
        return transactionRepository.findAllByUserIdAndType(securityContextHelper.getLoggedInUser().getId(), transactionType)
                .stream()
                .map(applicationMapper::transactionToTransactionDto)
                .toList();
    }

    @Override
    public void setTransactionAmountInternal(BigDecimal amount, Currency transactionCurrency, Currency userBaseCurrency, Transaction transaction) {
        transaction.setBaseAmount(amount);
        if (transactionCurrency == null || transactionCurrency.equals(userBaseCurrency)) {
            transaction.setBaseCurrency(userBaseCurrency);
            transaction.setAmount(amount);
        } else {
            transaction.setBaseCurrency(transactionCurrency);
            transaction.setAmount(currencyService.convert(
                    amount,
                    transactionCurrency,
                    userBaseCurrency,
                    LocalDate.now(ZoneId.of("Europe/Kyiv"))
            ));
        }
    }

    public void updateAmountAfterUserBaseCurrencyChange(Currency currencyToConvert) {
        CustomUserDetails loggedInUser = securityContextHelper.getLoggedInUser();
        List<Transaction> userTransactions = transactionRepository.findAllByUserId(loggedInUser.getId());
        for (Transaction transaction : userTransactions) {
            if (transaction.getBaseCurrency().equals(currencyToConvert)) {
                transaction.setAmount(transaction.getBaseAmount());
            } else {
                transaction.setAmount(currencyService.convert(transaction.getBaseAmount(), transaction.getBaseCurrency(), currencyToConvert, transaction.getTransactionDate()));
            }
        }
        transactionRepository.saveAll(userTransactions);
    }
}
