package ru.practicum.ewm.category.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryValidation {

    CategoryRepository categoryRepository;

    @Transactional
    public void existId(int id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(String.format("Категория с id %s не найдена", id));
        }
    }

    public void checkUniqNameCategoryIgnoreCase(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException(("Категория " + name + " уже существует"));
        }
    }
}
