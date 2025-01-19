package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.DuplicationException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.CategoryRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CategoryService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ApplicationMapper applicationMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, SecurityContextHelper securityContextHelper, ApplicationMapper applicationMapper) {
        this.categoryRepository = categoryRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
    }

    @Transactional
    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        securityContextHelper.validateLoggedInUser();
        Category category = applicationMapper.categoryDtoToCategory(categoryDto);
        category.setId(UUID.randomUUID());
        category.setCreatedAt(OffsetDateTime.now());
        category.setUserId(securityContextHelper.getLoggedInUser().getId());
        try {
            Category savedCategory = categoryRepository.save(category);
            log.info("CategoryService save : category successfully saved : {}", savedCategory);
            return applicationMapper.categoryToCategoryDto(savedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("CategoryService save : Unique constraint violation for user_id: {}, name: {}, type: {}",
                    category.getUserId(), category.getName(), category.getType(), e);
            throw new DuplicationException("Category with the same name and type already exists for user.");
        }
    }

    @Transactional
    @Override
    public CategoryDto renameCategory(UUID id, String name) {
        securityContextHelper.validateLoggedInUser();
        Category category = getCategory(id);
        category.setName(name);
        category.setUpdatedAt(OffsetDateTime.now());

        try {
            Category updatedCategory = categoryRepository.save(category);
            log.info("CategoryService renameCategory : category successfully updated : {}", updatedCategory);
            return applicationMapper.categoryToCategoryDto(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("CategoryService renameCategory : Unique constraint violation while renaming category ID: {}, " +
                    "new name: {}, user_id: {}, type: {}", id, name, category.getUserId(), category.getType(), e);
            throw new DuplicationException("Category with the same name and type already exists for user.");
        }
    }

    @Transactional
    @Override
    public void deleteCategory(UUID id) {
        securityContextHelper.validateLoggedInUser();
        categoryRepository.deleteByIdAndUserId(id, securityContextHelper.getLoggedInUser().getId());
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
}
