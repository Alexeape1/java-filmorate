package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    Collection<User> findAll();

    Optional<User> findById(Long id);

    User create(User user);

    User update(User user);

    void delete(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriends(Long userId);

    Collection<User> getCommonFriends(Long userId, Long otherId);
}