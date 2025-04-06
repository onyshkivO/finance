package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.DuplicationException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.exception.UnsupportedException;
import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.request.UpdateCategoryRequest;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.CategoryMcc;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.CategoryMccRepository;
import com.onyshkiv.finance.repository.CategoryRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CategoryService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMccRepository categoryMccRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ApplicationMapper applicationMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMccRepository categoryMccRepository, SecurityContextHelper securityContextHelper, ApplicationMapper applicationMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMccRepository = categoryMccRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
    }

    @Transactional
    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        securityContextHelper.validateLoggedInUser();
        UUID userId = securityContextHelper.getLoggedInUser().getId();

        checkExistingCategoriesOnDuplication(userId, categoryDto.getName(), TransactionType.valueOf(categoryDto.getType()));

        Category category = applicationMapper.categoryDtoToCategory(categoryDto);
        category.setId(UUID.randomUUID());
        category.setUserId(userId);
        category.setCreatedAt(OffsetDateTime.now());

        Set<Integer> newMccCodes = categoryDto.getMccCodes();

        List<Integer> existingDuplicateMccCodes = categoryMccRepository.findDuplicateMccCodes(userId, category.getType(), newMccCodes);
        if (!existingDuplicateMccCodes.isEmpty()) {
            log.error("CategoryService save : Duplicate MCC codes found for user_id: {}, type: {}, mcc_codes: {}",
                    userId, category.getType(), existingDuplicateMccCodes);
            throw new DuplicationException("Some of the following MCC codes are already assigned to another category: " + existingDuplicateMccCodes);
        }

        Set<CategoryMcc> categoryMccs = getCategoryMccs(newMccCodes, category, userId);
        category.setCategoryMccs(categoryMccs);

        Category savedCategory = categoryRepository.save(category);
        log.info("CategoryService save : category successfully saved : {}", savedCategory);

        return applicationMapper.categoryToCategoryDto(savedCategory);
    }


    private Set<CategoryMcc> getCategoryMccs(Set<Integer> mccCodes, Category category, UUID userId) {
        mccCodes = mccCodes != null ? mccCodes : Collections.emptySet();
        Set<CategoryMcc> categoryMccs = mccCodes.stream()
                .map(mcc -> CategoryMcc.builder()
                        .id(UUID.randomUUID())
                        .categoryId(category.getId())
                        .userId(userId)
                        .type(category.getType())
                        .mccCode(mcc)
                        .build())
                .collect(Collectors.toSet());
        return categoryMccs;
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(UUID id, UpdateCategoryRequest updateCategoryRequest) {
        securityContextHelper.validateLoggedInUser();
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        Category category = getCategory(id);

        checkExistingCategoriesOnDuplication(id, updateCategoryRequest.getName(), userId, category.getType());

        Set<Integer> newMccCodes = updateCategoryRequest.getMccCodes();

        List<Integer> duplicateMccCodes = categoryMccRepository.findDuplicateMccCodes(userId, category.getType(), newMccCodes, id);
        if (!duplicateMccCodes.isEmpty()) {
            log.error("CategoryService save : Duplicate MCC codes found for user_id: {}, type: {}, mcc_codes: {}",
                    userId, category.getType(), duplicateMccCodes);
            throw new DuplicationException("The following MCC codes are already assigned to another category: " + duplicateMccCodes);
        }

        category.setName(updateCategoryRequest.getName());
        category.setIcon(updateCategoryRequest.getIcon());
        Set<CategoryMcc> categoryMccs = getCategoryMccs(newMccCodes, category, userId);
        category.updateMccSet(categoryMccs);

        Category updatedCategory = categoryRepository.save(category);
        log.info("CategoryService updateCategory: category successfully updated: {}", updatedCategory);

        return applicationMapper.categoryToCategoryDto(updatedCategory);
    }

    @Transactional
    @Override
    public void deleteCategory(UUID id) {
        securityContextHelper.validateLoggedInUser();
        categoryRepository.deleteById(id);
        log.info("CategoryService deleteCategory : Category successfully deleted with id : {}", id);
    }

    @Override
    public CategoryDto getCategoryById(UUID id) {
        securityContextHelper.validateLoggedInUser();
        Category category = getCategory(id);
        return applicationMapper.categoryToCategoryDto(category);
    }

    @Override
    public Category getCategory(UUID id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isEmpty()) {
            log.error("CategoryService : Category not found with id {}", id);
            throw new NotFoundException(String.format("Category not found with id %s", id));
        }
        return categoryOptional.get();
    }

    @Override
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(applicationMapper::categoryToCategoryDto)
                .toList();
    }

    @Override
    public List<CategoryDto> getUserCategories(TransactionType transactionType) {
        securityContextHelper.validateLoggedInUser();
        return categoryRepository.findAllByUserIdAndType(securityContextHelper.getLoggedInUser().getId(), transactionType)
                .stream()
                .map(applicationMapper::categoryToCategoryDto)
                .toList();
    }

    @Override
    public boolean validateCategoryType(UUID categoryId, TransactionType type) {
        Category category = getCategory(categoryId);
        if (!category.getType().equals(type)) {
            log.warn("CategoryService : category type {} differ with type {}",
                    category.getType(), type);
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void transferCategoryTransactions(UUID categoryIdFrom, UUID categoryIdTo, TransactionType transactionType) {
        if ((categoryIdFrom != null && !getCategory(categoryIdFrom).getType().equals(transactionType))
                || (categoryIdTo != null && !getCategory(categoryIdTo).getType().equals(transactionType))) {
            throw new UnsupportedException("Impossible to transfer to different category type");
        }
        categoryRepository.transferCategoryTransactions(categoryIdFrom, categoryIdTo, transactionType.name());
    }

    private void checkExistingCategoriesOnDuplication(UUID userId, String name, TransactionType transactionType) {
        Optional<Category> existingCategory = categoryRepository.getByUserIdAndNameAndType(userId, name, transactionType);
        if (existingCategory.isPresent()) {
            log.error("CategoryService save : Unique constraint violation for user_id: {}, name: {}, type: {}",
                    userId, name, transactionType);
            throw new DuplicationException("Category with the same name and type already exists for user.");
        }
    }

    private void checkExistingCategoriesOnDuplication(UUID categoryId, String name, UUID userId, TransactionType transactionType) {
        Optional<Category> existingCategory = categoryRepository.getByUserIdAndNameAndType(userId, name, transactionType);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
            log.error("CategoryService update : Unique constraint violation for user_id: {}, name: {}, type: {}",
                    userId, name, transactionType);
            throw new DuplicationException("Category with the same name and type already exists for user.");
        }
    }
}
