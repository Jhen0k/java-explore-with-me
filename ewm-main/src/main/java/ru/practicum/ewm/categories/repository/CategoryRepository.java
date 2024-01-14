package ru.practicum.ewm.categories.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.categories.model.Category;


import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> getCategoriesBy(Pageable pageable);
}
