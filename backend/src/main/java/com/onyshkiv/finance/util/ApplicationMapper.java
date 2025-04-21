package com.onyshkiv.finance.util;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankAccountDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankCardResponse;
import com.onyshkiv.finance.model.dto.monobank.MonobankClientDto;
import com.onyshkiv.finance.model.dto.request.CashboxRequest;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.dto.response.CashboxResponse;
import com.onyshkiv.finance.model.dto.response.ExtendedCashboxResponse;
import com.onyshkiv.finance.model.dto.response.TransferResponse;
import com.onyshkiv.finance.model.entity.Cashbox;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.CategoryMcc;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.MonobankAccount;
import com.onyshkiv.finance.model.entity.MonobankAccountType;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.Transfer;
import com.onyshkiv.finance.model.entity.User;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.onyshkiv.finance.util.Helper.convertCurrencyCodeToCurrency;

@Mapper(componentModel = "spring")
public abstract class ApplicationMapper {
    public abstract User signUpRequestToUser(SignUpRequest signUpRequest);

    public abstract Category categoryDtoToCategory(CategoryDto categoryDto);

    public CategoryDto categoryToCategoryDto(Category category) {
        return category == null ? null : CategoryDto.builder()
                .id(category.getId())
                .type(category.getType().name())
                .name(category.getName())
                .icon(category.getIcon())
                .mccCodes(category.getCategoryMccs().stream().map(CategoryMcc::getMccCode).collect(Collectors.toSet()))
                .build();
    }

    public abstract Transaction transactionDtoToTransaction(TransactionDto transactionDto);

    public TransactionDto transactionToTransactionDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDto.TransactionDtoBuilder transactionDto = TransactionDto.builder();

        transactionDto.id(transaction.getId());
        transactionDto.category(categoryToCategoryDto(transaction.getCategory()));
        if (transaction.getType() != null) {
            transactionDto.type(transaction.getType().name());
        }
        transactionDto.amount(transaction.getAmount());
        transactionDto.currency(transaction.getBaseCurrency());
        transactionDto.description(transaction.getDescription());
        transactionDto.transactionDate(transaction.getTransactionDate());
        transactionDto.cashbox(cashboxToCashboxResponse(transaction.getCashbox()));

        return transactionDto.build();
    }

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
                .cashbackType(account.getCashbackType())
                .maskedPan(account.getMaskedPan().get(0))//todo if list is empty???
                .balance(account.getBalance().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .build()).toList();
    }

    public MonobankCardResponse monobankAccountToMonobankCardResponse(MonobankAccount monobankAccount) {
        return MonobankCardResponse.builder()
                .id(monobankAccount.getAccountId())
                .type(monobankAccount.getType().name())
                .currencyCode(convertCurrencyCodeToCurrency(monobankAccount.getCurrencyCode()).name())
                .iban(monobankAccount.getIban())
                .isMonitoring(monobankAccount.getMonitor())
                .maskedPan(monobankAccount.getMaskedPan())
                .cashboxId(monobankAccount.getCashbox().getId())
                .build();
    }


    public Cashbox cashboxRequestToCashbox(CashboxRequest cashboxRequest) {
        return Cashbox.builder()
                .id(UUID.randomUUID())
                .name(cashboxRequest.getName())
                .currency(Currency.fromCode(cashboxRequest.getCurrency().toUpperCase()))
                .balance(cashboxRequest.getBalance())
                .build();
    }

    public CashboxResponse cashboxToCashboxResponse(Cashbox cashbox) {
        return CashboxResponse.builder()
                .id(cashbox.getId())
                .userId(cashbox.getUserId())
                .name(cashbox.getName())
                .balance(cashbox.getBalance())
                .currency(cashbox.getCurrency().name())
                .build();
    }

    public TransferResponse transferToTransferResponse(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .amountFrom(transfer.getFromAmount())
                .amountTo(transfer.getToAmount())
                .cashboxFrom(cashboxToCashboxResponse(transfer.getFromCashbox()))
                .cashboxTo(cashboxToCashboxResponse(transfer.getToCashbox()))
                .date(transfer.getDate())
                .description(transfer.getDescription())
                .build();
    }

    public ExtendedCashboxResponse cashboxToExtendedCashboxResponse(Cashbox cashbox) {
        return ExtendedCashboxResponse.builder()
                .id(cashbox.getId())
                .userId(cashbox.getUserId())
                .name(cashbox.getName())
                .balance(cashbox.getBalance())
                .currency(cashbox.getCurrency().name())
                .transactions(cashbox.getTransactions().stream().map(this::transactionToTransactionDto).toList())
                .transfers(cashbox.getTransfers().stream().map(this::transferToTransferResponse).toList())
                .build();
    }
}
