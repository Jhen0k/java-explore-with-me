package ru.practicum.ewm.category.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryValidation {

    CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Category getCategoryAfterCheck(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException(
                String.format("Категория c id= %s не найдена", catId)));
    }
}
