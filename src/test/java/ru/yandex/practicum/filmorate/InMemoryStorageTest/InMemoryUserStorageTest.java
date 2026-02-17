package ru.yandex.practicum.filmorate.InMemoryStorageTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryUserStorageTest {

    private InMemoryUserStorage userStorage;
    private User validUser;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();

        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testLogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1993, 7, 17));
    }

    @Test
    void createUserSuccess() {
        User createdUser = userStorage.create(validUser);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testLogin", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
    }

    @Test
    void createUserWithDuplicateEmail() {
        userStorage.create(validUser);

        User duplicateUser = new User();
        duplicateUser.setEmail("test@example.com");
        duplicateUser.setLogin("anotherLogin");
        duplicateUser.setName("Another User");
        duplicateUser.setBirthday(LocalDate.of(1991, 1, 1));

        assertThrows(ValidationException.class, () -> userStorage.create(duplicateUser));
    }

    @Test
    void findAllUsers() {
        userStorage.create(validUser);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherLogin");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1991, 1, 1));
        userStorage.create(anotherUser);

        Collection<User> allUsers = userStorage.findAll();

        assertEquals(2, allUsers.size());
    }

    @Test
    void findUserByIdSuccess() {
        User createdUser = userStorage.create(validUser);

        var foundUser = userStorage.findById(createdUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(createdUser.getId(), foundUser.get().getId());
    }

    @Test
    void findUserByIdNotFound() {
        Optional<User> foundUser = userStorage.findById(987L);

        assertFalse(foundUser.isPresent());
    }

    @Test
    void updateUserSuccess() {
        User createdUser = userStorage.create(validUser);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");

        User updatedUser = userStorage.update(createdUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void updateUserWithDuplicateEmail() {
        User user1 = userStorage.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);

        createdUser2.setEmail("test@example.com");

        assertThrows(ValidationException.class, () -> userStorage.update(createdUser2));
    }

    @Test
    void updateNonExistentUser() {
        validUser.setId(999L);

        assertThrows(NotFoundException.class, () -> userStorage.update(validUser));
    }

    @Test
    void deleteUserSuccess() {
        User createdUser = userStorage.create(validUser);

        userStorage.delete(createdUser.getId());

        Optional<User> foundUser = userStorage.findById(createdUser.getId());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void addFriendSuccess() {
        User user1 = userStorage.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(user1.getId(), createdUser2.getId());

        Set<Long> user1Friends = userStorage.getFriends(user1.getId());
        Set<Long> user2Friends = userStorage.getFriends(createdUser2.getId());

        assertEquals(1, user1Friends.size());
        assertEquals(1, user2Friends.size());
        assertTrue(user1Friends.contains(createdUser2.getId()));
        assertTrue(user2Friends.contains(user1.getId()));
    }

    @Test
    void addFriendWithNonExistentUser() {
        User createdUser = userStorage.create(validUser);

        assertThrows(NotFoundException.class,
                () -> userStorage.addFriend(createdUser.getId(), 999L));
    }

    @Test
    void removeFriendSuccess() {
        User user1 = userStorage.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(user1.getId(), createdUser2.getId());
        userStorage.removeFriend(user1.getId(), createdUser2.getId());

        Set<Long> user1Friends = userStorage.getFriends(user1.getId());
        Set<Long> user2Friends = userStorage.getFriends(createdUser2.getId());

        assertEquals(0, user1Friends.size());
        assertEquals(0, user2Friends.size());
    }

    @Test
    void getCommonFriends() {
        User user1 = userStorage.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@example.com");
        commonFriend.setLogin("common");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        User createdCommonFriend = userStorage.create(commonFriend);

        userStorage.addFriend(user1.getId(), createdCommonFriend.getId());
        userStorage.addFriend(createdUser2.getId(), createdCommonFriend.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(
                user1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(createdCommonFriend.getId(),
                commonFriends.iterator().next().getId());
    }

    @Test
    void getCommonFriendsNoCommon() {
        User user1 = userStorage.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);

        User friend1 = new User();
        friend1.setEmail("friend1@example.com");
        friend1.setLogin("friend1");
        friend1.setName("Friend 1");
        friend1.setBirthday(LocalDate.of(1992, 1, 1));
        User createdFriend1 = userStorage.create(friend1);

        User friend2 = new User();
        friend2.setEmail("friend2@example.com");
        friend2.setLogin("friend2");
        friend2.setName("Friend 2");
        friend2.setBirthday(LocalDate.of(1993, 1, 1));
        User createdFriend2 = userStorage.create(friend2);

        userStorage.addFriend(user1.getId(), createdFriend1.getId());
        userStorage.addFriend(createdUser2.getId(), createdFriend2.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(
                user1.getId(), createdUser2.getId());

        assertTrue(commonFriends.isEmpty());
    }
}