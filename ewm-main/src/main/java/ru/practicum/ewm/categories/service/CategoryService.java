package ru.practicum.ewm.categories.service;

import ru.practicum.ewm.categories.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(CategoryDto categoryDto, int catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoriesById(int id);

    void deleteCategory(int catId);
}
