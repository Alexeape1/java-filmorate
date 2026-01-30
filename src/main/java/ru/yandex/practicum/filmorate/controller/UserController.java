package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Создание пользователя: {}", user.getEmail());

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Попытка создания пользователя с пустым email");
            throw new ValidationException("email не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Попытка создания пользователя с email, не содержащим символ: @ (ID: {})", user.getId());
            throw new ValidationException("email должен содержать символ @");
        }
        boolean emailExists = users.values().stream()
                .anyMatch(existingUser -> user.getEmail().equals(existingUser.getEmail()));
        if (emailExists) {
            log.warn("Попытка создания уже существующего email: {} (ID: {})", user.getEmail(), user.getId());
            throw new ValidationException("Этот email уже используется");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Попытка создания пустого логина");
            throw new ValidationException("логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Попытка создания пользователя с использованием пробелов в логине");
            throw new ValidationException("логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.warn("Попытка создания пользователя без указания даты рождения");
            throw new ValidationException("дата рождения не может быть пустой");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Попытка создания пользователя с датой рождения в будущем: {} (ID: {})",
                    user.getBirthday(), user.getId());
            throw new ValidationException("дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан с ID: {}", user.getId());
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя. ID: {}, Email: {}",
                newUser.getId(), newUser.getEmail());

        if (newUser.getId() == null) {
            log.warn("Попытка обновления пользователя без указания id");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с ID: {}", newUser.getId());
            throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null) {
            if (!newUser.getEmail().contains("@")) {

                log.warn("Попытка обновления пользователя с некорректным email: {} (ID: {})",
                        newUser.getEmail(), newUser.getId());
                throw new ValidationException("email должен содержать символ @");
            }
            if (newUser.getEmail().isBlank()) {
                log.warn("Попытка обновления пользователя с пустым email: {} (ID: {}", newUser.getEmail(),
                        newUser.getId());
                throw new ValidationException("email не может быть пустой");
            }
            boolean emailExists = users.values().stream()
                    .filter(u -> !u.getId().equals(newUser.getId()))
                    .anyMatch(u -> newUser.getEmail().equalsIgnoreCase(u.getEmail()));

            if (emailExists) {
                log.warn("Попытка обновления email на уже существующий: {} (ID: {})", newUser.getEmail(), newUser.getId());
                throw new ValidationException("Этот email уже используется другим пользователем");
            }
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank()) {
                log.warn("Попытка обновления пользователя с пустым логином (ID: {})", newUser.getId());
                throw new ValidationException("логин не может быть пустым");
            }
            if (newUser.getLogin().contains(" ")) {
                log.warn("Попытка обновления пользователя с логином, содержащим пробелы: {} (ID: {})",
                        newUser.getLogin(), newUser.getId());
                throw new ValidationException("логин не может содержать пробелы");
            }
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            if (newUser.getName().isBlank()) {
                String loginToUse = newUser.getLogin() != null ? newUser.getLogin() : oldUser.getLogin();
                oldUser.setName(loginToUse);
                log.debug("Имя пользователя пустое, будет использован логин: {} (ID: {})",
                        loginToUse, newUser.getId());
            } else {
                oldUser.setName(newUser.getName());
            }
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Попытка обновления пользователя с датой рождения в будущем: {} (ID: {})",
                        newUser.getBirthday(), newUser.getId());
                throw new ValidationException("дата рождения не может быть в будущем");
            }
            oldUser.setBirthday(newUser.getBirthday());
        }

        log.info("Пользователь успешно обновлен. ID: {}, Email: {}", newUser.getId(), newUser.getEmail());
        return oldUser;
    }
}