package ru.practicum.ewm.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.validation.UserValidation;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    UserValidation userValidation;

    @Override
    public UserDto createUser(UserDto userDto) {
        return userMapper.toDto(userRepository.save(userMapper.toEntity(userDto)));
    }

    @Override
    public List<UserDto> getUsers(Integer ids, Integer from, Integer size) {
        List<UserDto> userDtoList = new ArrayList<>();
        if (ids != null) {
            if (!userRepository.existsById(ids)) {
                return new ArrayList<>();
            } else {
                UserDto userDto = userMapper.toDto(userRepository.findById(ids).orElseThrow());
                userDtoList.add(userDto);
                return userDtoList;
            }
        } else {
            Pageable pageable = Paginator.getPageable(from, size);
            return userMapper.toDtoList(userRepository.getAllBy(pageable));
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        userValidation.checkUserById(userId);
        userRepository.deleteById(userId);
    }
}
