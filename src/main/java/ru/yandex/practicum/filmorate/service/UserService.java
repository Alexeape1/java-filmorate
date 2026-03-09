package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.request.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    public Collection<UserDto> getAllUsers() {
        return userStorage.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto create(CreateUserRequest request) {
        log.info("Создание пользователя: {}", request.getEmail());

        validateUser(request);

        User user = userMapper.toEntity(request);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User created = userStorage.create(user);

        return userMapper.toDto(created);
    }

    public UserDto update(UpdateUserRequest request) {
        log.info("Обновление пользователя с id: {}", request.getId());
        User existingUser = userStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + request.getId() + " не найден"));

        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            validateEmail(request.getEmail());
        }

        if (request.getLogin() != null && !request.getLogin().equals(existingUser.getLogin())) {
            validateLogin(request.getLogin());
        }

        if (request.getBirthday() != null) {
            validateBirthday(request.getBirthday());
        }

        User updatedUser = userMapper.updateEntity(existingUser, request);

        if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
            updatedUser.setName(updatedUser.getLogin());
        }

        User saved = userStorage.update(updatedUser);

        return userMapper.toDto(saved);
    }

    public UserDto findById(Long id) {
        log.info("Поиск пользователя по id: {}", id);
        return userStorage.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление друга {} пользователю {}", friendId, userId);
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        findById(userId);
        findById(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление друга {} у пользователя {}", friendId, userId);
        findById(userId);
        findById(friendId);

        userStorage.removeFriend(userId, friendId);
    }

    public Collection<UserDto> getFriends(Long userId) {
        log.info("Получение друзей пользователя с id: {}", userId);

        findById(userId);
        Set<Long> friendIds = userStorage.getFriends(userId);

        return friendIds.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public Collection<UserDto> getCommonFriends(Long userId, Long otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);
        findById(userId);
        findById(otherId);

        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateUser(CreateUserRequest user) {
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        validateBirthday(user.getBirthday());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Некорректный email");
        }
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank() || login.contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}