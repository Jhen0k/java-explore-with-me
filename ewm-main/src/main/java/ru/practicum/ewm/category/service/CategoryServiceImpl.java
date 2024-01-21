package ru.practicum.ewm.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.validation.CategoryValidation;

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
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, int catId) {
        categoryValidation.existId(catId);
        Category oldCategory = categoryRepository.findById(catId).orElseThrow();
        if (categoryDto.getName() != null && !oldCategory.getName().equals(categoryDto.getName())) {
            categoryValidation.checkUniqNameCategoryIgnoreCase(categoryDto.getName());
        }
        oldCategory.setName(categoryDto.getName());

        return categoryMapper.toDto(categoryRepository.save(oldCategory));
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
