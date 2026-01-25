package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.Exception.ValidationException;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {  // Думаю тут можно разделить на 2 класса тестов для UserController и FilmController

    private UserController userController;
    private FilmController filmController;

    private final String film = "Film";
    private final String description = "Description";
    private final String email = "java@mail.com";
    private final String login = "Login";


    @BeforeEach
    void setUp() {
        userController = new UserController();
        filmController = new FilmController();
    }

    @Test
    void createFilm_WithValidData_ShouldCreateFilm() {

        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Film", created.getName());
        assertEquals("Description", created.getDescription());
        assertEquals(120, created.getDuration());
    }

    @Test
    void createFilm_WithEmptyName_ShouldThrowException() {
        Film film = new Film();
        film.setName("");
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));

        assertEquals("название не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_WithNullName_ShouldThrowException() {
        Film film = new Film();
        film.setName(null);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));

        assertEquals("название не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_WithBlankName_ShouldThrowException() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));

        assertEquals("название не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_WithTooLongDescription_ShouldThrowException() {
        Film film = new Film();
        film.setName(this.film);
        String longDescription = "F".repeat(201); // 201 символ
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));

        assertTrue(exception.getMessage().contains("максимальная длина описания"));
    }

    @Test
    void createFilm_WithDescriptionExactly200Chars_ShouldBeOk() {
        Film film = new Film();
        film.setName(this.film);
        String exactDescription = "F".repeat(200); // Ровно 200 символов
        film.setDescription(exactDescription);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        assertEquals(exactDescription, created.getDescription());
    }

    @Test
    void createFilm_WithEmptyDescription_ShouldBeOk() {
        Film film = new Film();
        film.setName(this.film);
        film.setDescription("");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        assertNotNull(created);
        assertEquals("", created.getDescription());
    }

    @Test
    void createFilm_WithReleaseDateBefore1895_ShouldThrowException() {
        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertTrue(exception.getMessage().contains("дата релиза — не раньше"));
    }

    @Test
    void createFilm_WithReleaseDateExactly1895_ShouldBeOk() {
        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldThrowException() {
        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2014, 11, 15));
        film.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilm_WithZeroDuration_ShouldThrowException() {
        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));

        assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName(film);
        film1.setDescription(description);
        film1.setReleaseDate(LocalDate.of(2010, 10, 10));
        film1.setDuration(100);
        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2011, 11, 11));
        film2.setDuration(111);

        filmController.createFilm(film1);
        filmController.createFilm(film2);
        Collection<Film> allFilms = filmController.findAll();

        assertEquals(2, allFilms.size());
    }

    @Test
    void findAll_WhenNoFilms_ShouldReturnEmptyList() {
        Collection<Film> allFilms = filmController.findAll();

        assertTrue(allFilms.isEmpty());
    }

    @Test
    void updateFilm_WithValidData_ShouldUpdateFilm() {

        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(created.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setDuration(150);

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals(created.getId(), result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    @Test
    void updateFilm_WithoutId_ShouldThrowException() {
        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(film));

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void updateFilm_WithInvalidData_ShouldThrowException() {

        Film film = new Film();
        film.setName(this.film);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(created.getId());
        updatedFilm.setName(""); // Пустое имя
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setDuration(150);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(updatedFilm));

        assertEquals("название не может быть пустым", exception.getMessage());
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