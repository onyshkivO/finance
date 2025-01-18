package com.onyshkiv.finance.util;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.TransactionDto;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    User signUpRequestToUser(SignUpRequest signUpRequest);

    Category categoryDtoToCategory(CategoryDto categoryDto);

    CategoryDto categoryToCategoryDto(Category category);

    Transaction transactionDtoToTransaction(TransactionDto transactionDto);

    TransactionDto transactionToTransactionDto(Transaction transaction);
}
