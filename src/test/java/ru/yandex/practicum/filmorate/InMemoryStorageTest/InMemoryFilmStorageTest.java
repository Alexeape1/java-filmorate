package ru.yandex.practicum.filmorate.InMemoryStorageTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InMemoryFilmStorageTest {

    private InMemoryFilmStorage filmStorage;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Test Description");
        validFilm.setReleaseDate(LocalDate.of(2014, 9, 20));
        validFilm.setDuration(123);
    }

    @Test
    void createFilmSuccess() {
        Film createdFilm = filmStorage.create(validFilm);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals(123, createdFilm.getDuration());
    }

    @Test
    void createFilmWithDuplicateName() {
        filmStorage.create(validFilm);

        Film duplicateFilm = new Film();
        duplicateFilm.setName("Test Film");
        duplicateFilm.setDescription("Another Description");
        duplicateFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        duplicateFilm.setDuration(130);

        assertThrows(ValidationException.class, () -> filmStorage.create(duplicateFilm));
    }

    @Test
    void findAllFilms() {
        filmStorage.create(validFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(130);
        filmStorage.create(anotherFilm);

        Collection<Film> allFilms = filmStorage.findAll();

        assertEquals(2, allFilms.size());
    }

    @Test
    void findFilmByIdSuccess() {
        Film createdFilm = filmStorage.create(validFilm);

        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertTrue(foundFilm.isPresent());
        assertEquals(createdFilm.getId(), foundFilm.get().getId());
    }

    @Test
    void findFilmByIdNotFound() {
        var foundFilm = filmStorage.findById(999L);

        assertFalse(foundFilm.isPresent());
    }

    @Test
    void updateFilmSuccess() {
        Film createdFilm = filmStorage.create(validFilm);
        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");

        Film updatedFilm = filmStorage.update(createdFilm);

        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
    }

    @Test
    void updateNonExistentFilm() {
        validFilm.setId(999L);

        assertThrows(NotFoundException.class, () -> filmStorage.update(validFilm));
    }

    @Test
    void deleteFilmSuccess() {
        Film createdFilm = filmStorage.create(validFilm);

        filmStorage.delete(createdFilm.getId());

        var foundFilm = filmStorage.findById(createdFilm.getId());
        assertFalse(foundFilm.isPresent());
    }

    @Test
    void addLikeSuccess() {
        Film createdFilm = filmStorage.create(validFilm);

        filmStorage.addLike(createdFilm.getId(), 1L);
        filmStorage.addLike(createdFilm.getId(), 2L);

        Film film = filmStorage.findById(createdFilm.getId()).get();
        assertEquals(2, film.getLikes().size());
        assertTrue(film.getLikes().contains(1L));
        assertTrue(film.getLikes().contains(2L));
    }

    @Test
    void removeLikeSuccess() {
        Film createdFilm = filmStorage.create(validFilm);
        filmStorage.addLike(createdFilm.getId(), 1L);
        filmStorage.addLike(createdFilm.getId(), 2L);

        filmStorage.removeLike(createdFilm.getId(), 1L);

        Film film = filmStorage.findById(createdFilm.getId()).get();
        assertEquals(1, film.getLikes().size());
        assertFalse(film.getLikes().contains(1L));
        assertTrue(film.getLikes().contains(2L));
    }

    @Test
    void getPopularFilms() {
        Film film1 = filmStorage.create(validFilm);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(130);
        filmStorage.create(film2);

        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2002, 1, 1));
        film3.setDuration(140);
        filmStorage.create(film3);

        filmStorage.addLike(film1.getId(), 1L);
        filmStorage.addLike(film1.getId(), 2L);
        filmStorage.addLike(film2.getId(), 1L);

        Collection<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.iterator().next().getId());
    }

    @Test
    void getPopularFilmsWithCountGreaterThanTotal() {
        filmStorage.create(validFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(130);
        filmStorage.create(anotherFilm);

        Collection<Film> popularFilms = filmStorage.getPopularFilms(10);

        assertEquals(2, popularFilms.size());
    }
}