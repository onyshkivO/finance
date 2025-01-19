package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.TransactionRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CategoryService;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, SecurityContextHelper securityContextHelper, ApplicationMapper applicationMapper, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
        this.categoryService = categoryService;
    }


    @Transactional
    @Override
    public TransactionDto save(TransactionDto transactionDto) {
        securityContextHelper.validateLoggedInUser();
        Transaction transaction = applicationMapper.transactionDtoToTransaction(transactionDto);
        if (!categoryService.validateCategoryType(transactionDto.getCategory().getId(), transaction.getType())) {
            throw new IllegalStateException(String.format("category type differ with type %s",//todo change exception
                    transaction.getType()));
        }
        transaction.setId(UUID.randomUUID());
        transaction.setCategory(new Category(transactionDto.getCategory().getId()));
        transaction.setUserId(securityContextHelper.getLoggedInUser().getId());
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("TransactionService save : transaction successfully saved : {}", savedTransaction);
        return applicationMapper.transactionToTransactionDto(transaction);
    }

    //todo maybe add table where will be save all user income and vitrat sum(think we should not create it but do it using cache)
    @Transactional
    @Override
    public TransactionDto updateTransaction(UUID id, TransactionDto transactionDto) {
        securityContextHelper.validateLoggedInUser();
        Transaction transaction = getTransaction(id);
        if (!categoryService.validateCategoryType(transactionDto.getCategory().getId(), transaction.getType())) {
            throw new IllegalStateException(String.format("category type differ with type %s",//todo change exception
                    transaction.getType()));
        }

        transaction.setTransactionDate(transactionDto.getTransactionDate());
        transaction.setCategory(new Category(transactionDto.getCategory().getId()));
        transaction.setAmount(transactionDto.getAmount());
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
    @Transactional
    public void moveTransactionsToAnotherCategory(UUID sourceCategoryId, UUID targetCategoryId) {
        //validation if category exists
        categoryService.getCategory(sourceCategoryId);
        categoryService.getCategory(targetCategoryId);

        transactionRepository.moveTransactionsToAnotherCategory(sourceCategoryId, targetCategoryId);
    }
}
