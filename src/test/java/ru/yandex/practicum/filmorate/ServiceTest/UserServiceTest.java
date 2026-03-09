package ru.yandex.practicum.filmorate.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.request.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserMapper userMapper;
    private CreateUserRequest validRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userMapper = new UserMapper();
        userService = new UserService(userStorage, userMapper);

        validRequest = new CreateUserRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setLogin("testLogin");
        validRequest.setName("Test User");
        validRequest.setBirthday(LocalDate.of(1995, 10, 30));

        updateRequest = new UpdateUserRequest();
    }

    @Test
    void createUserSuccess() {
        UserDto createdUser = userService.create(validRequest);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testLogin", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
    }

    @Test
    void createUserWithEmptyEmail() {
        validRequest.setEmail("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validRequest));

        assertEquals("Некорректный email", exception.getMessage());
    }

    @Test
    void createUserWithInvalidEmail() {
        validRequest.setEmail("invalid-email"); // Используем validRequest

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validRequest));

        assertEquals("Некорректный email", exception.getMessage());
    }

    @Test
    void createUserWithEmptyLogin() {
        validRequest.setLogin(""); // Используем validRequest

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validRequest));

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithLoginContainingSpaces() {
        validRequest.setLogin("login with spaces"); // Используем validRequest

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validRequest));

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithFutureBirthday() {
        validRequest.setBirthday(LocalDate.now().plusDays(1)); // Используем validRequest

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(validRequest));

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void createUserWithEmptyNameShouldUseLogin() {
        validRequest.setName(""); // Используем validRequest

        UserDto createdUser = userService.create(validRequest);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void createUserWithNullNameShouldUseLogin() {
        validRequest.setName(null); // Используем validRequest

        UserDto createdUser = userService.create(validRequest);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void updateUserSuccess() {
        UserDto createdUser = userService.create(validRequest);

        updateRequest.setId(createdUser.getId());
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setLogin("updatedLogin");
        updateRequest.setBirthday(LocalDate.of(1990, 1, 1));

        UserDto updatedUser = userService.update(updateRequest);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("updatedLogin", updatedUser.getLogin());
        assertEquals(LocalDate.of(1990, 1, 1), updatedUser.getBirthday());
    }

    @Test
    void updateUserPartialUpdate() {
        UserDto createdUser = userService.create(validRequest);

        updateRequest.setId(createdUser.getId());
        updateRequest.setName("Updated Name");

        UserDto updatedUser = userService.update(updateRequest);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("test@example.com", updatedUser.getEmail());
        assertEquals("testLogin", updatedUser.getLogin());
        assertEquals(LocalDate.of(1995, 10, 30), updatedUser.getBirthday());
    }

    @Test
    void findUserByIdSuccess() {
        UserDto createdUser = userService.create(validRequest);

        UserDto foundUser = userService.findById(createdUser.getId());

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
        userService.create(validRequest);

        CreateUserRequest anotherRequest = new CreateUserRequest();
        anotherRequest.setEmail("another@example.com");
        anotherRequest.setLogin("anotherUser");
        anotherRequest.setName("Another User");
        anotherRequest.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(anotherRequest);

        Collection<UserDto> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void addFriendSuccess() {
        UserDto user1 = userService.create(validRequest);

        CreateUserRequest user2Request = new CreateUserRequest();
        user2Request.setEmail("user2@example.com");
        user2Request.setLogin("user2");
        user2Request.setName("User 2");
        user2Request.setBirthday(LocalDate.of(1991, 1, 1));
        UserDto user2 = userService.create(user2Request);

        userService.addFriend(user1.getId(), user2.getId());

        Collection<UserDto> friends = userService.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.iterator().next().getId());
    }

    @Test
    void addSelfAsFriend() {
        UserDto createdUser = userService.create(validRequest);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.addFriend(createdUser.getId(), createdUser.getId()));

        assertEquals("Нельзя добавить самого себя в друзья", exception.getMessage());
    }

    @Test
    void addFriendToNonExistentUser() {
        UserDto createdUser = userService.create(validRequest);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.addFriend(createdUser.getId(), 111L));

        assertEquals("Пользователь с id=111 не найден", exception.getMessage());
    }

    @Test
    void removeFriendSuccess() {
        UserDto user1 = userService.create(validRequest);

        CreateUserRequest user2Request = new CreateUserRequest();
        user2Request.setEmail("user2@example.com");
        user2Request.setLogin("user2");
        user2Request.setName("User 2");
        user2Request.setBirthday(LocalDate.of(1991, 1, 1));
        UserDto user2 = userService.create(user2Request);

        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());

        Collection<UserDto> friends = userService.getFriends(user1.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getFriendsSuccess() {
        UserDto user1 = userService.create(validRequest);

        CreateUserRequest user2Request = new CreateUserRequest();
        user2Request.setEmail("user2@example.com");
        user2Request.setLogin("user2");
        user2Request.setName("User 2");
        user2Request.setBirthday(LocalDate.of(1991, 1, 1));
        UserDto user2 = userService.create(user2Request);

        CreateUserRequest user3Request = new CreateUserRequest();
        user3Request.setEmail("user3@example.com");
        user3Request.setLogin("user3");
        user3Request.setName("User 3");
        user3Request.setBirthday(LocalDate.of(1992, 1, 1));
        UserDto user3 = userService.create(user3Request);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        Collection<UserDto> friends = userService.getFriends(user1.getId());

        assertEquals(2, friends.size());
        boolean hasUser2 = friends.stream().anyMatch(u -> u.getId().equals(user2.getId()));
        boolean hasUser3 = friends.stream().anyMatch(u -> u.getId().equals(user3.getId()));
        assertTrue(hasUser2);
        assertTrue(hasUser3);
    }

    @Test
    void getFriendsEmpty() {
        UserDto createdUser = userService.create(validRequest);

        Collection<UserDto> friends = userService.getFriends(createdUser.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriends() {
        UserDto user1 = userService.create(validRequest);

        CreateUserRequest user2Request = new CreateUserRequest();
        user2Request.setEmail("user2@example.com");
        user2Request.setLogin("user2");
        user2Request.setBirthday(LocalDate.of(1991, 1, 1));
        UserDto user2 = userService.create(user2Request);

        CreateUserRequest commonRequest = new CreateUserRequest();
        commonRequest.setEmail("common@example.com");
        commonRequest.setLogin("common");
        commonRequest.setBirthday(LocalDate.of(1992, 1, 1));
        UserDto commonFriend = userService.create(commonRequest);

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        Collection<UserDto> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.iterator().next().getId());
    }

    @Test
    void getCommonFriendsWithNoCommonFriends() {
        UserDto user1 = userService.create(validRequest);

        CreateUserRequest user2Request = new CreateUserRequest();
        user2Request.setEmail("user2@example.com");
        user2Request.setLogin("user2");
        user2Request.setBirthday(LocalDate.of(1991, 1, 1));
        UserDto user2 = userService.create(user2Request);

        Collection<UserDto> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }
}