package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
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

        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
            log.warn("Попытка обновления пользователя с некорректным email: {} (ID: {})",
                    newUser.getEmail(), newUser.getId());
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        boolean emailExists = users.values().stream()
                .filter(u -> !u.getId().equals(newUser.getId()))
                .anyMatch(u -> newUser.getEmail().equalsIgnoreCase(u.getEmail()));

        if (emailExists) {
            log.warn("Попытка обновления email на уже существующий: {} (ID: {})", newUser.getEmail(), newUser.getId());
            throw new ValidationException("Этот email уже используется другим пользователем");
        }

        oldUser.setEmail(newUser.getEmail());
        oldUser.setName(newUser.getName());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());

        log.info("Пользователь успешно обновлен. ID: {}, Email: {}", newUser.getId(), newUser.getEmail());
        return oldUser;
    }
}