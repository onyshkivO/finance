package com.onyshkiv.finance.util;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankClientDto;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.entity.*;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ApplicationMapper {
    public abstract User signUpRequestToUser(SignUpRequest signUpRequest);

    public abstract Category categoryDtoToCategory(CategoryDto categoryDto);

    public CategoryDto categoryToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .type(category.getType().name())
                .name(category.getName())
                .mccCodes(category.getCategoryMccs().stream().map(CategoryMcc::getMccCode).collect(Collectors.toSet()))
                .build();
    }

    public abstract Transaction transactionDtoToTransaction(TransactionDto transactionDto);

    public abstract TransactionDto transactionToTransactionDto(Transaction transaction);

    public List<MonobankAccount> monobankClientDtoToMonobankAccountList(MonobankClientDto monobankClientDto, UUID userId) {
        return monobankClientDto.getAccounts().stream().map(account -> MonobankAccount.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .clientId(monobankClientDto.getClientId())
                .name(monobankClientDto.getName())
                .accountId(account.getId())
                .sendId(account.getSendId())
                .iban(account.getIban())
                .currencyCode(account.getCurrencyCode())
                .type(MonobankAccountType.fromString(account.getType()))
                .build()).toList();
    }
}
