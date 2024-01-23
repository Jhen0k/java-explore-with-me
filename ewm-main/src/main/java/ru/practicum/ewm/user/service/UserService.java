package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserService {
    UserDto postUser(UserDto userDto);

    void deleteUser(long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    User checkExistUser(long userId);
}