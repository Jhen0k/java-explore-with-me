package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(CategoryDto categoryDto, int catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoriesById(int id);

    void deleteCategory(int catId);
}
