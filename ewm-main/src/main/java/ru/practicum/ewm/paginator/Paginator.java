package ru.practicum.ewm.paginator;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.exception.ValidationException;

public abstract class Paginator {

    public static Pageable getPageable(Integer from, Integer size, String properties) {
        if (from == null || from < 0) throw new ValidationException("Параметр from не может быть меньше 0");
        if (size == null || size <= 0) throw new ValidationException("Параметр size не может быть меньше 0");

        int page = from / size;

        Sort sort = Sort.by(Sort.Direction.ASC, properties);

        return PageRequest.of(page, size, sort);
    }

    public static Pageable getPageable(Integer from, Integer size) {
        if (from == null || from < 0) throw new ValidationException("Параметр from не может быть меньше 0");
        if (size == null || size <= 0) throw new ValidationException("Параметр size не может быть меньше 0");

        int page = from / size;

        return PageRequest.of(page, size);
    }
}
