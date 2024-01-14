package ru.practicum.ewm.categories.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.mapper.CategoryMapper;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.categories.validation.CategoryValidation;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryValidation categoryValidation;
    CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, int catId) {
        categoryValidation.existId(catId);
        Category category = categoryRepository.findById(catId).orElseThrow();
        category.setName(categoryDto.getName());

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = Paginator.getPageable(from, size);

        return categoryMapper.toListDto(categoryRepository.getCategoriesBy(pageable));
    }

    @Override
    @Transactional
    public CategoryDto getCategoriesById(int catId) {
        categoryValidation.existId(catId);

        return categoryMapper.toDto(categoryRepository.findById(catId).orElseThrow());
    }

    @Override
    @Transactional
    public void deleteCategory(int catId) {
        categoryValidation.existId(catId);
        categoryRepository.deleteById(catId);
    }
}
