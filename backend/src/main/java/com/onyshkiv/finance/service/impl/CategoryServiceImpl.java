package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.repository.CategoryRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.CategoryService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CategoryDto save(CategoryDto categoryDto) {
        securityContextHelper.validateLoggedInUser();
        Category category = applicationMapper.categoryDtoToCategory(categoryDto);
        category.setId(UUID.randomUUID());
        category.setCreatedAt(OffsetDateTime.now());
        category.setUserId(securityContextHelper.getLoggedInUser().getId());
        Category savedCategory = categoryRepository.save(category);
        log.info("CategoryService save : category successfully saved : {}", savedCategory);
        return applicationMapper.categoryToCategoryDto(category);
    }

    @Transactional
    public CategoryDto renameCategory(UUID id, String name) {
        securityContextHelper.validateLoggedInUser();
        Category category = getCategory(id);
        category.setName(name);
        category.setUpdatedAt(OffsetDateTime.now());
        return applicationMapper.categoryToCategoryDto(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        securityContextHelper.validateLoggedInUser();
        categoryRepository.deleteByIdAndUserId(id, securityContextHelper.getLoggedInUser().getId());
        log.info("CategoryService deleteCategory : Category successfully deleted with id : {}", id);
    }

    public CategoryDto getCategoryById(UUID id) {
        Category category = getCategory(id);
        return applicationMapper.categoryToCategoryDto(category);
    }

    public Category getCategory(UUID id) {
        securityContextHelper.validateLoggedInUser();
        Optional<Category> categoryOptional = categoryRepository.findByIdAndUserId(id, securityContextHelper.getLoggedInUser().getId());
        if (categoryOptional.isEmpty()) {
            log.error("CategoryService renameCategory : Category not found with id {} for user {}", id, securityContextHelper.getLoggedInUser().getLogin());
            throw new NotFoundException(String.format("Category not found with id %s for user %s", id, securityContextHelper.getLoggedInUser().getLogin()));
        }
        return categoryOptional.get();
    }

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(applicationMapper::categoryToCategoryDto)
                .toList();
    }

    public List<CategoryDto> getUserCategories() {
        securityContextHelper.validateLoggedInUser();
        return categoryRepository.findAllByUserId(securityContextHelper.getLoggedInUser().getId())
                .stream()
                .map(applicationMapper::categoryToCategoryDto)
                .toList();
    }
}
