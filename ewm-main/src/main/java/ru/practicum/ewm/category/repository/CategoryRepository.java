package ru.practicum.ewm.category.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.category.model.Category;


import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> getCategoriesBy(Pageable pageable);

    Boolean existsByNameIgnoreCase(String name);
}
