package ru.practicum.ewm.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.validation.CategoryValidation;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.category.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryValidation categoryValidation;
    CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryDto postCategory(CategoryDto categoryDto) {
        Category category = categoryMapper.toCategory(categoryDto);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public void deleteCategory(long catId) {
        try {
            categoryRepository.deleteById(catId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Категория c id= %s не найдена", catId));
        }
    }

    @Transactional
    @Override
    public CategoryDto patchCategory(long catId, CategoryDto categoryDto) {
        Category categoryUpdate = categoryMapper.toCategory(categoryDto);
        Category categoryOld = categoryValidation.getCategoryAfterCheck(catId);
        categoryUpdate.setId(categoryOld.getId());

        return categoryMapper.toCategoryDto(categoryRepository.save(categoryUpdate));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = Paginator.getPageable(from, size);
        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.stream().map(categoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryById(long catId) {
        Category category = categoryValidation.getCategoryAfterCheck(catId);

        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public Category checkExistCategory(long catId) {
        return categoryValidation.getCategoryAfterCheck(catId);
    }
}
