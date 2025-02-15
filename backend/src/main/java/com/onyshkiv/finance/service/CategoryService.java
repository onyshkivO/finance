package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.request.UpdateCategoryRequest;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.TransactionType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryDto save(CategoryDto categoryDto);

    CategoryDto updateCategory(UUID id, UpdateCategoryRequest updateCategoryRequest);

    void deleteCategory(UUID id);

    CategoryDto getCategoryById(UUID id);

    Category getCategory(UUID id);

    List<CategoryDto> getCategories();

    List<CategoryDto> getUserCategories(TransactionType transactionType);

    boolean validateCategoryType(UUID categoryId, TransactionType type);

    void transferCategoryTransactions(UUID categoryIdFrom, UUID categoryIdTo, TransactionType transactionType);
}
