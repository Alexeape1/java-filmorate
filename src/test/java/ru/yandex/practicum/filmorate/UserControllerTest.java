package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.Exception.ValidationException;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private UserController userController;

    private final String email = "java@mail.com";
    private final String login = "Login";

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1998, 7, 17));

        User created = userController.createUser(user);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("java@mail.com", created.getEmail());
        assertEquals("Login", created.getLogin());
        assertEquals("Test User", created.getName());
    }

    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertEquals("Login", created.getName());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowException() {
        User user = new User();
        user.setEmail(""); // Пустой email
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertTrue(exception.getMessage().contains("email не может быть пустой"));
    }

    @Test
    void createUser_WithEmailWithoutAtSymbol_ShouldThrowException() {
        User user = new User();
        user.setEmail("invalid-email.com"); // Нет @
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertTrue(exception.getMessage().contains("email должен содержать символ @"));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {

        User user1 = new User();
        user1.setEmail(email);
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userController.createUser(user1);

        User user2 = new User();
        user2.setEmail(email);
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user2));

        assertEquals("Этот email уже используется", exception.getMessage());
    }

    @Test
    void createUser_WithLoginContainingSpaces_ShouldThrowException() {
        User user = new User();
        user.setEmail(email);
        user.setLogin("log in"); // Логин с пробелом
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void createUser_WithFutureBirthday_ShouldThrowException() {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@email.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userController.createUser(user1);
        userController.createUser(user2);

        Collection<User> allUsers = userController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {

        User user = new User();
        user.setEmail("original@email.com");
        user.setLogin("originallogin");
        user.setName("Original Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        User updatedUser = new User();
        updatedUser.setId(created.getId());
        updatedUser.setEmail("updated@email.com");
        updatedUser.setLogin("updatedlogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(1991, 1, 1));

        User result = userController.updateUser(updatedUser);

        assertEquals(created.getId(), result.getId());
        assertEquals("updated@email.com", result.getEmail());
        assertEquals("updatedlogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void updateUser_WithoutId_ShouldThrowException() {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.updateUser(user));

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowException() {

        User user1 = new User();
        user1.setEmail("user1@email.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userController.createUser(user1);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        userController.createUser(user2);

        User updateRequest = new User();
        updateRequest.setId(created1.getId());
        updateRequest.setEmail("user2@email.com"); // Email уже занят
        updateRequest.setLogin("login1");
        updateRequest.setName("Updated User 1");
        updateRequest.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.updateUser(updateRequest));

        assertEquals("Этот email уже используется другим пользователем", exception.getMessage());
    }
}