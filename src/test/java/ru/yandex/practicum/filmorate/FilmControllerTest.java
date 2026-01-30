package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.Exception.ValidationException;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private FilmController filmController;

    private final String film = "Film";
    private final String description = "Description";

    @BeforeEach
    void setUp() {

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
}