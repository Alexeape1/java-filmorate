package ru.yandex.practicum.filmorate.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;
    private Film validFilm;
    private User validUser;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, userStorage);

        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Test Description");
        validFilm.setReleaseDate(LocalDate.of(2002, 2, 2));
        validFilm.setDuration(120);

        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testLogin");
        validUser.setBirthday(LocalDate.of(1991, 1, 1));
    }

    @Test
    void createFilmSuccess() {
        Film createdFilm = filmService.create(validFilm);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals(120, createdFilm.getDuration());
    }

    @Test
    void createFilmWithEmptyName() {
        validFilm.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validFilm));

        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilmWithTooLongDescription() {
        String longDescription = "A".repeat(201);
        validFilm.setDescription(longDescription);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validFilm));

        assertEquals("Описание не может превышать 200 символов", exception.getMessage());
    }

    @Test
    void createFilmWithNullReleaseDate() {
        validFilm.setReleaseDate(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validFilm));

        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    @Test
    void createFilmWithFutureReleaseDate() {
        validFilm.setReleaseDate(LocalDate.of(2030, 10, 10));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.create(validFilm));

        assertEquals("Дата релиза не может быть в будущем", exception.getMessage());
    }

    @Test
    void createFilmWithNegativeDuration() {
        validFilm.setDuration(-1);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.create(validFilm));

        assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
    }

    @Test
    void updateFilmSuccess() {
        Film createdFilm = filmService.create(validFilm);
        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");

        Film updatedFilm = filmService.update(createdFilm);

        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
    }

    @Test
    void updateNonExistentFilm() {
        validFilm.setId(12345L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.update(validFilm));

        assertEquals("Фильм с id=12345 не найден", exception.getMessage());
    }

    @Test
    void findFilmByIdSuccess() {
        Film createdFilm = filmService.create(validFilm);

        Film foundFilm = filmService.findById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void findAllFilms() {
        filmService.create(validFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(130);
        filmService.create(anotherFilm);

        Collection<Film> allFilms = filmService.findAll();

        assertEquals(2, allFilms.size());
    }

    @Test
    void addLikeSuccess() {
        Film createdFilm = filmService.create(validFilm);
        User createdUser = userService.create(validUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());

        Film filmAfterLike = filmService.findById(createdFilm.getId());
        assertTrue(filmAfterLike.getLikes().contains(createdUser.getId()));
    }

    @Test
    void removeLikeSuccess() {
        Film createdFilm = filmService.create(validFilm);
        User createdUser = userService.create(validUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());
        filmService.removeLike(createdFilm.getId(), createdUser.getId());

        Film filmAfterUnlike = filmService.findById(createdFilm.getId());
        assertFalse(filmAfterUnlike.getLikes().contains(createdUser.getId()));
    }

    @Test
    void getPopularFilms() {
        Film film1 = filmService.create(validFilm);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(130);
        filmService.create(film2);

        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2002, 1, 1));
        film3.setDuration(140);
        filmService.create(film3);

        User user1 = userService.create(validUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(user2);

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));
        userService.create(user3);

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film1.getId(), user3.getId());

        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        filmService.addLike(film3.getId(), user1.getId());

        Collection<Film> popularFilms = filmService.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.iterator().next().getId());

        Collection<Film> top1 = filmService.getPopularFilms(1);
        assertEquals(1, top1.size());
        assertEquals(film1.getId(), top1.iterator().next().getId());
    }

    @Test
    void getPopularFilmsWhenNoLikes() {
        filmService.create(validFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(130);
        filmService.create(anotherFilm);

        Collection<Film> popularFilms = filmService.getPopularFilms(10);

        assertEquals(2, popularFilms.size());
    }
}