package ru.practicum.ewm.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    @Transactional
    @Override
    public UserDto postUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Category with id=" + userId + " was not found");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = Paginator.getPageable(from, size);
        Page<User> users;

        if (ids == null) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findAllByIdIn(ids, pageable);
        }

        return users.stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public User checkExistUser(long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException("User with id= " + userId + " was not found");
        } else {
            return user.get();
        }
    }
}
