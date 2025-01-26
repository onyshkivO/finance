package com.onyshkiv.finance.util;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankClientDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDto;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.entity.*;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class ApplicationMapper {
    public abstract User signUpRequestToUser(SignUpRequest signUpRequest);

    public abstract Category categoryDtoToCategory(CategoryDto categoryDto);

    public abstract CategoryDto categoryToCategoryDto(Category category);

    public abstract Transaction transactionDtoToTransaction(TransactionDto transactionDto);

    public abstract TransactionDto transactionToTransactionDto(Transaction transaction);

    public List<MonobankAccount> monobankClientDtoToMonobankAccountList(MonobankClientDto monobankClientDto, String requestId) {
        return monobankClientDto.getAccounts().stream().map(account -> MonobankAccount.builder()
                .id(UUID.randomUUID())
                .requestId(requestId)
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
