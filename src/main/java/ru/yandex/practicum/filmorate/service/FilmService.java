package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.request.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final RatingStorage ratingStorage;
    private final FilmMapper filmMapper;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage,
                       @Qualifier("ratingDbStorage") RatingStorage ratingStorage,
                       FilmMapper filmMapper) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
        this.filmMapper = filmMapper;
    }

    public Collection<FilmDto> findAll() {
        return filmStorage.findAll().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto create(CreateFilmRequest request) {
        log.info("Создание фильма: {}", request.getName());
        log.info("Запрос: mpa={}, genres={}", request.getMpa(), request.getGenres());

        validateCreateRequest(request);

        Film film = filmMapper.toEntity(request);

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            Integer mpaId = request.getMpa().getId();
            log.info("Загружаем MPA с id: {}", mpaId);
            Rating rating = ratingStorage.findById(mpaId)
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг MPA с id=" + mpaId + " не найден"));
            film.setMpa(rating);
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            log.info("Загружаем жанры: {}", request.getGenres());
            Set<Genre> genres = request.getGenres().stream()
                    .map(genreDto -> {
                        Genre genre = genreStorage.findById(genreDto.getId())
                                .orElseThrow(() -> new NotFoundException(
                                        "Жанр с id=" + genreDto.getId() + " не найден"));
                        return genre;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(genres);
        } else {
            film.setGenres(new HashSet<>());
        }

        Film created = filmStorage.create(film);
        return filmMapper.toDto(created);
    }

    public FilmDto update(UpdateFilmRequest request) {
        log.info("Обновление фильма с id: {}", request.getId());

        Film existingFilm = filmStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + request.getId() + " не найден"));

        if (request.getName() != null && !request.getName().equals(existingFilm.getName())) {
            checkFilmNameUnique(request.getName(), request.getId());
        }

        if (request.getName() != null) {
            existingFilm.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingFilm.setDescription(request.getDescription());
        }
        if (request.getReleaseDate() != null) {
            existingFilm.setReleaseDate(request.getReleaseDate());
        }
        if (request.getDuration() > 0) {
            existingFilm.setDuration(request.getDuration());
        }

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            Integer mpaId = request.getMpa().getId();
            log.info("Обновляем MPA на id: {}", mpaId);
            Rating rating = ratingStorage.findById(mpaId)
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг MPA с id=" + mpaId + " не найден"));
            existingFilm.setMpa(rating);
        }

        if (request.getGenres() != null) {
            if (request.getGenres().isEmpty()) {
                existingFilm.setGenres(new HashSet<>());
                log.info("Удаляем все жанры");
            } else {
                log.info("Обновляем жанры: {}", request.getGenres());
                Set<Genre> genres = request.getGenres().stream()
                        .map(genreDto -> {
                            Genre genre = genreStorage.findById(genreDto.getId())
                                    .orElseThrow(() -> new NotFoundException(
                                            "Жанр с id=" + genreDto.getId() + " не найден"));
                            return genre;
                        })
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                existingFilm.setGenres(genres);
            }
        }

        validateFilm(existingFilm);

        Film saved = filmStorage.update(existingFilm);
        log.info("Фильм обновлен: id={}, name={}, mpaId={}, genres={}",
                saved.getId(),
                saved.getName(),
                saved.getMpa() != null ? saved.getMpa().getId() : null,
                saved.getGenres() != null ? saved.getGenres().stream().map(Genre::getId).collect(Collectors.toList()) : null);

        return filmMapper.toDto(saved);
    }

    public FilmDto findById(Long id) {
        log.info("Поиск фильма по id: {}", id);
        FilmDto film = filmStorage.findById(id)
                .map(filmMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));

        return film;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} пользователем {}", filmId, userId);
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма {} пользователем {}", filmId, userId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        log.info("Получение {} популярных фильмов", count);
        return filmStorage.getPopularFilms(count).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateCreateRequest(CreateFilmRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (request.getDescription() != null && request.getDescription().length() > 200) {
            throw new ValidationException("Описание не может превышать 200 символов");
        }
        if (request.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (request.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
        if (request.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (request.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        if (request.getMpa() == null || request.getMpa().getId() == null) {
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может превышать 200 символов");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
    }

    private void checkFilmNameUnique(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return;
        }
        boolean exists = filmStorage.findAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(name)
                        && (excludeId == null || !f.getId().equals(excludeId)));

        if (exists) {
            throw new ValidationException("Фильм с названием '" + name + "' уже существует");
        }
    }
}