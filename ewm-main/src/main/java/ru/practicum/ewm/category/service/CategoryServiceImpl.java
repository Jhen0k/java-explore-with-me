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
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.category.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
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
    public void deleteCategory(long id) {
        try {
            categoryRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
    }

    @Transactional
    @Override
    public CategoryDto patchCategory(long id, CategoryDto categoryDto) {
        Category categoryUpdate = categoryMapper.toCategory(categoryDto);
        Optional<Category> categoryOldOpt = categoryRepository.findById(id);

        if (categoryOldOpt.isEmpty()) {
            throw new NotFoundException("Category with id=" + id + "was not found");
        }

        Category categoryOld = categoryOldOpt.get();
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
        Optional<Category> category = categoryRepository.findById(catId);

        if (category.isEmpty()) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }

        return categoryMapper.toCategoryDto(category.get());
    }

    @Transactional(readOnly = true)
    @Override
    public Category checkExistCategory(long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);

        if (category.isEmpty()) {
            throw new NotFoundException("Category with id=" + categoryId + " was not found");
        } else {
            return category.get();
        }
    }
}
