package ru.yandex.practicum.filmorate.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private User validUser;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testLogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1995, 10, 30));
    }

    @Test
    void createUserSuccess() {
        User createdUser = userService.create(validUser);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testLogin", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
    }

    @Test
    void createUserWithEmptyEmail() {
        validUser.setEmail("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validUser));

        assertEquals("Некорректный email", exception.getMessage());
    }

    @Test
    void createUserWithInvalidEmail() {
        validUser.setEmail("invalid-email");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validUser));

        assertEquals("Некорректный email", exception.getMessage());
    }

    @Test
    void createUserWithEmptyLogin() {
        validUser.setLogin("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validUser));

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithLoginContainingSpaces() {
        validUser.setLogin("login with spaces");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validUser));

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithFutureBirthday() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validUser));

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void createUserWithEmptyNameShouldUseLogin() {
        validUser.setName("");

        User createdUser = userService.create(validUser);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void createUserWithNullNameShouldUseLogin() {
        validUser.setName(null);

        User createdUser = userService.create(validUser);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void updateUserSuccess() {
        User createdUser = userService.create(validUser);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");

        User updatedUser = userService.update(createdUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void findUserByIdSuccess() {
        User createdUser = userService.create(validUser);

        User foundUser = userService.findById(createdUser.getId());

        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findNonExistentUser() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findById(999L));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void getAllUsers() {
        userService.create(validUser);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherUser");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(anotherUser);

        Collection<User> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void addFriendSuccess() {
        User user1 = userService.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());

        Collection<User> friends = userService.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(createdUser2.getId(), friends.iterator().next().getId());
    }

    @Test
    void addSelfAsFriend() {
        User createdUser = userService.create(validUser);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.addFriend(createdUser.getId(), createdUser.getId()));

        assertEquals("Нельзя добавить самого себя в друзья", exception.getMessage());
    }

    @Test
    void addFriendToNonExistentUser() {
        User createdUser = userService.create(validUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.addFriend(createdUser.getId(), 111L));

        assertEquals("Пользователь с id=111 не найден", exception.getMessage());
    }

    @Test
    void removeFriendSuccess() {
        User user1 = userService.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());
        userService.removeFriend(user1.getId(), createdUser2.getId());

        Collection<User> friends = userService.getFriends(user1.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getFriendsSuccess() {
        User user1 = userService.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setName("User 3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));
        User createdUser3 = userService.create(user3);

        userService.addFriend(user1.getId(), createdUser2.getId());
        userService.addFriend(user1.getId(), createdUser3.getId());

        Collection<User> friends = userService.getFriends(user1.getId());

        assertEquals(2, friends.size());
        boolean hasUser2 = friends.stream().anyMatch(u -> u.getId().equals(createdUser2.getId()));
        boolean hasUser3 = friends.stream().anyMatch(u -> u.getId().equals(createdUser3.getId()));
        assertTrue(hasUser2);
        assertTrue(hasUser3);
    }

    @Test
    void getFriendsEmpty() {
        User createdUser = userService.create(validUser);

        Collection<User> friends = userService.getFriends(createdUser.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriends() {
        User user1 = userService.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@example.com");
        commonFriend.setLogin("common");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        userService.create(commonFriend);

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.iterator().next().getId());
    }

    @Test
    void getCommonFriendsWithNoCommonFriends() {
        User user1 = userService.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(user2);

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }
}