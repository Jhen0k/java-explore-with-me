package ru.practicum.ewm.user.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.validation.ValidationException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserValidation {

    UserRepository userRepository;

    public void checkUserById(Integer ids) {
        if (!userRepository.existsById(ids)) {
            throw new ValidationException(String.format("Пользователь с id %s не найден", ids));
        }
    }
}
