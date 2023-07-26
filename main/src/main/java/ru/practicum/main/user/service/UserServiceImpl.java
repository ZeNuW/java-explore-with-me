package ru.practicum.main.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.mapper.UserMapper;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        return UserMapper.userToDto(userRepository.save(UserMapper.userFromUserCreateRequestDto(newUserRequest)));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(PageRequest.of(from, size)).getContent()
                    .stream()
                    .map(UserMapper::userToDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, PageRequest.of(from, size)).getContent()
                    .stream()
                    .map(UserMapper::userToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.delete(getUser(userId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ObjectValidationException(String.format("Пользователь с id = %d не найден", userId)));
    }
}