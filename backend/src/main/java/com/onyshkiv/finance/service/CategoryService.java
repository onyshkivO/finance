package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.entity.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryDto save(CategoryDto categoryDto);

    CategoryDto renameCategory(UUID id, String name);

    void deleteCategory(UUID id);

    CategoryDto getCategoryById(UUID id);

    Category getCategory(UUID id);

    List<CategoryDto> getCategories();

    List<CategoryDto> getUserCategories();
}
