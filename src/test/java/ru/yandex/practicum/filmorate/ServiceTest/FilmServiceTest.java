package ru.yandex.practicum.filmorate.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.request.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.request.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryRatingStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;
    private FilmMapper filmMapper;
    private UserMapper userMapper;
    private CreateFilmRequest validRequest;
    private UpdateFilmRequest updateRequest;
    private CreateUserRequest validUserRequest;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryGenreStorage genreStorage = new InMemoryGenreStorage();
        InMemoryRatingStorage ratingStorage = new InMemoryRatingStorage();

        filmMapper = new FilmMapper();
        userMapper = new UserMapper();

        userService = new UserService(userStorage, userMapper);
        filmService = new FilmService(filmStorage, userStorage, genreStorage, ratingStorage, filmMapper);

        validRequest = new CreateFilmRequest();
        validRequest.setName("Test Film");
        validRequest.setDescription("Test Description");
        validRequest.setReleaseDate(LocalDate.of(2002, 2, 2));
        validRequest.setDuration(120);
    RatingDto ratingDto = new RatingDto();
        ratingDto.setId(1);
        validRequest.setMpa(ratingDto);

        GenreDto genreDto = new GenreDto();
        genreDto.setId(1);
        validRequest.setGenres(List.of(genreDto));

        updateRequest = new UpdateFilmRequest();

        validUserRequest = new CreateUserRequest();
        validUserRequest.setEmail("test@example.com");
        validUserRequest.setLogin("testLogin");
        validUserRequest.setName("Test User");
        validUserRequest.setBirthday(LocalDate.of(1991, 1, 1));
    }

    @Test
    void createFilmSuccess() {
        FilmDto createdFilm = filmService.create(validRequest);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals(120, createdFilm.getDuration());

        assertNotNull(createdFilm.getMpa());
        assertEquals(1, createdFilm.getMpa().getId());
        assertEquals("G", createdFilm.getMpa().getName());

        assertNotNull(createdFilm.getGenres());
        assertEquals(1, createdFilm.getGenres().size());
        assertEquals(1, createdFilm.getGenres().get(0).getId());
        assertEquals("Комедия", createdFilm.getGenres().get(0).getName());
    }

    @Test
    void createFilmWithEmptyName() {
        validRequest.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validRequest));

        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilmWithTooLongDescription() {
        String longDescription = "A".repeat(201);
        validRequest.setDescription(longDescription);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validRequest));

        assertEquals("Описание не может превышать 200 символов", exception.getMessage());
    }

    @Test
    void createFilmWithNullReleaseDate() {
        validRequest.setReleaseDate(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validRequest));

        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    @Test
    void createFilmWithFutureReleaseDate() {
        validRequest.setReleaseDate(LocalDate.of(2030, 10, 10));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validRequest));

        assertEquals("Дата релиза не может быть в будущем", exception.getMessage());
    }

    @Test
    void createFilmWithNegativeDuration() {
        validRequest.setDuration(-1);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(validRequest));

        assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
    }

    @Test
    void updateFilmSuccess() {
        FilmDto createdFilm = filmService.create(validRequest);

        updateRequest.setId(createdFilm.getId());
        updateRequest.setName("Updated Film");
        updateRequest.setDescription("Updated Description");
        updateRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        updateRequest.setDuration(150);

        RatingDto ratingDto = new RatingDto();
        ratingDto.setId(5);
        updateRequest.setMpa(ratingDto);

        GenreDto genreDto = new GenreDto();
        genreDto.setId(2);
        updateRequest.setGenres(List.of(genreDto));

        FilmDto updatedFilm = filmService.update(updateRequest);

        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(150, updatedFilm.getDuration());

        assertNotNull(updatedFilm.getMpa());
        assertEquals(5, updatedFilm.getMpa().getId());
        assertEquals("NC-17", updatedFilm.getMpa().getName());

        assertNotNull(updatedFilm.getGenres());
        assertEquals(1, updatedFilm.getGenres().size());
        assertEquals(2, updatedFilm.getGenres().get(0).getId());
    }

    @Test
    void updateFilmPartialUpdate() {
        FilmDto createdFilm = filmService.create(validRequest);

        updateRequest.setId(createdFilm.getId());
        updateRequest.setName("Updated Film");

        FilmDto updatedFilm = filmService.update(updateRequest);

        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Test Description", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2002, 2, 2), updatedFilm.getReleaseDate());
        assertEquals(120, updatedFilm.getDuration());
        assertEquals(1, updatedFilm.getMpa().getId());
    }

    @Test
    void updateNonExistentFilm() {
        updateRequest.setId(12345L);
        updateRequest.setName("Updated Film");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.update(updateRequest));

        assertEquals("Фильм с id=12345 не найден", exception.getMessage());
    }

    @Test
    void findFilmByIdSuccess() {
        FilmDto createdFilm = filmService.create(validRequest);

        FilmDto foundFilm = filmService.findById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void findNonExistentFilm() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.findById(999L));

        assertEquals("Фильм с id=999 не найден", exception.getMessage());
    }

    @Test
    void findAllFilms() {
        filmService.create(validRequest);

        CreateFilmRequest anotherRequest = new CreateFilmRequest();
        anotherRequest.setName("Another Film");
        anotherRequest.setDescription("Another Description");
        anotherRequest.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherRequest.setDuration(130);

        RatingDto ratingDto = new RatingDto();
        ratingDto.setId(1);
        anotherRequest.setMpa(ratingDto);


        GenreDto genreDto = new GenreDto();
        genreDto.setId(1);
        anotherRequest.setGenres(List.of(genreDto));

        filmService.create(anotherRequest);

        Collection<FilmDto> allFilms = filmService.findAll();

        assertEquals(2, allFilms.size());
    }

    @Test
    void addLikeSuccess() {
        FilmDto createdFilm = filmService.create(validRequest);
        UserDto createdUser = userService.create(validUserRequest);

        filmService.addLike(createdFilm.getId(), createdUser.getId());

        FilmDto filmAfterLike = filmService.findById(createdFilm.getId());
        assertEquals(1, filmAfterLike.getLikesCount());
    }

    @Test
    void removeLikeSuccess() {
        FilmDto createdFilm = filmService.create(validRequest);
        UserDto createdUser = userService.create(validUserRequest);

        filmService.addLike(createdFilm.getId(), createdUser.getId());
        filmService.removeLike(createdFilm.getId(), createdUser.getId());

        FilmDto filmAfterUnlike = filmService.findById(createdFilm.getId());
        assertEquals(0, filmAfterUnlike.getLikesCount());
    }

    @Test
    void getPopularFilms() {
        FilmDto film1 = filmService.create(validRequest);

        CreateFilmRequest film2Request = new CreateFilmRequest();
        film2Request.setName("Film 2");
        film2Request.setDescription("Description 2");
        film2Request.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2Request.setDuration(130);

        RatingDto ratingDto2 = new RatingDto();
        ratingDto2.setId(1);
        film2Request.setMpa(ratingDto2);

        GenreDto genreDto2 = new GenreDto();
        genreDto2.setId(1);
        film2Request.setGenres(List.of(genreDto2));

        FilmDto film2 = filmService.create(film2Request);

        CreateFilmRequest film3Request = new CreateFilmRequest();
        film3Request.setName("Film 3");
        film3Request.setDescription("Description 3");
        film3Request.setReleaseDate(LocalDate.of(2002, 1, 1));
        film3Request.setDuration(140);

        RatingDto ratingDto3 = new RatingDto();
        ratingDto3.setId(1);
        film3Request.setMpa(ratingDto3);

        GenreDto genreDto3 = new GenreDto();
        genreDto3.setId(1);
        film3Request.setGenres(List.of(genreDto3));
        FilmDto film3 = filmService.create(film3Request);

        UserDto user1 = userService.create(validUserRequest);

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

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film1.getId(), user3.getId());

        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        filmService.addLike(film3.getId(), user1.getId());

        Collection<FilmDto> popularFilms = filmService.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.iterator().next().getId());

        Collection<FilmDto> top1 = filmService.getPopularFilms(1);
        assertEquals(1, top1.size());
        assertEquals(film1.getId(), top1.iterator().next().getId());
    }

    @Test
    void getPopularFilmsWhenNoLikes() {
        filmService.create(validRequest);

        CreateFilmRequest anotherRequest = new CreateFilmRequest();
        anotherRequest.setName("Another Film");
        anotherRequest.setDescription("Another Description");
        anotherRequest.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherRequest.setDuration(130);

        RatingDto ratingDto = new RatingDto();
        ratingDto.setId(1);
        anotherRequest.setMpa(ratingDto);

        GenreDto genreDto = new GenreDto();
        genreDto.setId(1);
        anotherRequest.setGenres(List.of(genreDto));

        filmService.create(anotherRequest);

        Collection<FilmDto> popularFilms = filmService.getPopularFilms(10);

        assertEquals(2, popularFilms.size());
    }
}