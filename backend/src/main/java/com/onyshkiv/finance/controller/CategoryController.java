package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.CategoryDto;
import com.onyshkiv.finance.model.dto.request.UpdateCategoryRequest;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid CategoryDto category) {
        return categoryService.save(category);
    }

    @PutMapping("rename/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto renameCategory(@PathVariable("id") UUID id, @RequestBody @Valid UpdateCategoryRequest updateCategoryRequest) {
        return categoryService.updateCategory(id, updateCategoryRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable("id") UUID id) {
        categoryService.deleteCategory(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable("id") UUID id) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping("/type/{transactionType}")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getUserCategories(@PathVariable TransactionType transactionType) {
        return categoryService.getUserCategories(transactionType);
    }

}
