package com.vj.ezybuy.products.service;

import com.vj.ezybuy.products.dto.CategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryDto> getAllCategories();

    CategoryDto getCategoryById(Long categoryId);

    List<CategoryDto> getCategoriesByProductId(UUID productId);

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    void deleteCategory(Long categoryId);
}
