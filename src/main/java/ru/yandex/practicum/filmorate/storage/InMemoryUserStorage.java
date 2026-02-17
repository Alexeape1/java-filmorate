package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    private final Map<Long, Set<Long>> friends = new HashMap<>();

    private long currentId = 1L;

    @Override
    public Collection<User> findAll() {
        log.info("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }

        user.setId(currentId++);
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        log.info("Создан пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        boolean emailExists = users.values().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Email " + user.getEmail() + " уже используется другим пользователем");
        }

        users.put(user.getId(), user);
        log.info("Обновлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        users.remove(id);
        friends.values().forEach(friendSet -> friendSet.remove(id));
        friends.remove(id);
        log.info("Удален пользователь с id: {}", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (friends.containsKey(userId)) {
            friends.get(userId).remove(friendId);
        }
        if (friends.containsKey(friendId)) {
            friends.get(friendId).remove(userId);
        }
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        validateUserExists(userId);
        return friends.getOrDefault(userId, new HashSet<>());
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId);
        validateUserExists(otherId);

        Set<Long> userFriends = getFriends(userId);
        Set<Long> otherFriends = getFriends(otherId);

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherFriends);

        return commonFriendIds.stream()
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}