package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.exception.UnsupportedException;
import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.entity.Cashbox;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.TransactionRepository;
import com.onyshkiv.finance.security.CustomUserDetails;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CashboxService;
import com.onyshkiv.finance.service.CategoryService;
import com.onyshkiv.finance.service.CurrencyService;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.onyshkiv.finance.model.entity.TransactionType.EXPENSE;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ApplicationMapper applicationMapper;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;
    private final CashboxService cashboxService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, SecurityContextHelper securityContextHelper, ApplicationMapper applicationMapper, CategoryService categoryService, CurrencyService currencyService, CashboxService cashboxService) {
        this.transactionRepository = transactionRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
        this.categoryService = categoryService;
        this.currencyService = currencyService;
        this.cashboxService = cashboxService;
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
        transaction.setCashbox(cashboxService.getCashbox(transactionDto.getCashbox().getId()));
        transaction.setUserId(loggedInUser.getId());

//        setTransactionAmountInternal(transactionDto.getAmount(), transactionDto.getCurrency(), loggedInUser.getCurrency(), transaction);
        transaction.setBaseCurrency(transactionDto.getCurrency());
        transaction.setBaseAmount(transactionDto.getAmount());
        transaction.setCoefficientCurrency(loggedInUser.getCurrency());
        transaction.setAmount(transactionDto.getCoefficient().multiply(transactionDto.getAmount()));
        Transaction savedTransaction = transactionRepository.save(transaction);

        applyTransactionToCashboxBalance(savedTransaction, loggedInUser.getCurrency());
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
//        if (!transaction.getAmount().equals(transactionDto.getAmount())) {
        rollbackTransactionFromCashboxBalance(transaction, loggedInUser.getCurrency());
//            setTransactionAmountInternal(transactionDto.getAmount(), transactionDto.getCurrency(), loggedInUser.getCurrency(), transaction);
        transaction.setBaseCurrency(transactionDto.getCurrency());
        transaction.setAmount(transactionDto.getCoefficient().multiply(transactionDto.getAmount()));
        transaction.setCoefficient(transactionDto.getCoefficient());
        transaction.setCoefficientCurrency(loggedInUser.getCurrency());
        transaction.setCashbox(cashboxService.getCashbox(transactionDto.getCashbox().getId()));
        applyTransactionToCashboxBalance(transaction, loggedInUser.getCurrency());
//        }

        transaction.setDescription(transactionDto.getDescription());

        return applicationMapper.transactionToTransactionDto(transaction);
    }

    private void applyTransactionToCashboxBalance(Transaction transaction, Currency userCurrency) {
        applyTransactionToCashbox(transaction, userCurrency, false);
    }

    private void rollbackTransactionFromCashboxBalance(Transaction transaction, Currency userCurrency) {
        applyTransactionToCashbox(transaction, userCurrency, true);
    }

    private void applyTransactionToCashbox(Transaction transaction, Currency userCurrency, boolean reverse) {
        Cashbox cashbox = transaction.getCashbox();
        BigDecimal balance = cashbox.getBalance();
        BigDecimal amount = calculateCurrencyAmountForTransactionCashbox(transaction, userCurrency);

        if (transaction.getType() == EXPENSE) {
//            if (balance.compareTo(amount) < 0 && !reverse){
//                throw new UnsupportedException("Cashbox balance is not enough!");
//            }
            balance = reverse ? balance.add(amount) : balance.subtract(amount);
        } else {
            balance = reverse ? balance.subtract(amount) : balance.add(amount);
        }

        cashbox.setBalance(balance);
    }

    private BigDecimal calculateCurrencyAmountForTransactionCashbox(Transaction transaction, Currency userCurrency) {
        Cashbox cashbox = transaction.getCashbox();
        BigDecimal amount;
        if (transaction.getBaseCurrency().equals(cashbox.getCurrency())) {
            amount = transaction.getBaseAmount();
        } else if (userCurrency.equals(cashbox.getCurrency())) {
            amount = transaction.getAmount();
        } else {
            amount = currencyService.convert(transaction.getAmount(), userCurrency, cashbox.getCurrency(), transaction.getTransactionDate());
        }
        return amount;
    }

    @Transactional
    @Override
    public void deleteTransaction(UUID id) {
        Currency userCurrency = securityContextHelper.getLoggedInUser().getCurrency();
        Transaction transaction = getTransaction(id);
        transactionRepository.deleteById(id);
        rollbackTransactionFromCashboxBalance(transaction, userCurrency);

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
    public List<TransactionDto> getUserTransactionsByDateRange(LocalDate from, LocalDate to) {
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        return transactionRepository.findUserTransactionByDateRange(userId, from, to)
                .stream()
                .map(transaction -> {
                    TransactionDto transactionDto = applicationMapper.transactionToTransactionDto(transaction);
                    if (transactionDto.getCategory() == null) {
                        transactionDto.setCategory(new CategoryDto(UUID.randomUUID(), "Other", transactionDto.getType(), null, Collections.emptySet()));
                    }
                    return transactionDto;
                })
                .toList();
    }

    @Override
    public void setTransactionAmountInternal(BigDecimal amount, Currency transactionCurrency, Currency userBaseCurrency, Transaction transaction) {
        transaction.setBaseAmount(amount);
        transaction.setCoefficientCurrency(userBaseCurrency);
        if (transactionCurrency == null || transactionCurrency.equals(userBaseCurrency)) {
            transaction.setBaseCurrency(userBaseCurrency);
            transaction.setCoefficient(BigDecimal.ONE);
            transaction.setAmount(amount);
        } else {
            BigDecimal exchangeRate = currencyService.getExchangeRate(transactionCurrency,
                    userBaseCurrency,
                    transaction.getTransactionDate());
            transaction.setBaseCurrency(transactionCurrency);
            transaction.setCoefficient(exchangeRate);
            transaction.setAmount(amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP));

//            transaction.setAmount(currencyService.convert(
//                    amount,
//                    transactionCurrency,
//                    userBaseCurrency,
//                    transaction.getTransactionDate()
//            ));
        }
    }

    public void updateAmountAfterUserBaseCurrencyChange(Currency currencyToConvert) {
        CustomUserDetails loggedInUser = securityContextHelper.getLoggedInUser();
        List<Transaction> userTransactions = transactionRepository.findAllByUserId(loggedInUser.getId());
        for (Transaction transaction : userTransactions) {
            if (transaction.getBaseCurrency().equals(currencyToConvert)) {
                transaction.setAmount(transaction.getBaseAmount());
            } else if (transaction.getCoefficientCurrency() != null && transaction.getCoefficient() != null && transaction.getCoefficientCurrency().equals(currencyToConvert)) {
                transaction.setAmount(transaction.getBaseAmount().multiply(transaction.getCoefficient()));
            } else {
                transaction.setAmount(currencyService.convert(transaction.getBaseAmount(), transaction.getBaseCurrency(), currencyToConvert, transaction.getTransactionDate()));
            }
        }
        transactionRepository.saveAll(userTransactions);
    }
}
