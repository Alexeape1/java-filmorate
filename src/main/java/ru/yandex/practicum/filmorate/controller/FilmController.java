package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final int MAX_DESCRIPTION = 200;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление нового фильма. Название: {}", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Попытка создания фильма с пустым названием");
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION) {
            log.warn("Попытка создания фильма с описанием длиной {} символов (максимум {})",
                    film.getDescription().length(), MAX_DESCRIPTION);
            throw new ValidationException("максимальная длина описания — " + MAX_DESCRIPTION + " символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Попытка создания фильма без даты релиза");
            throw new ValidationException("дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Попытка создания фильма с датой релиза {} (раньше {})", film.getReleaseDate(), CINEMA_BIRTHDAY);
            throw new ValidationException("дата релиза — не раньше " + CINEMA_BIRTHDAY);
        }
        if (film.getDuration() <= 0) {
            log.warn("Попытка создания фильма с отрицательной продолжительностью {}", film.getDuration());
            throw new ValidationException("продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан. ID: {}, Название: {}",
                film.getId(), film.getName());
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма. ID: {}, Name: {}",
                newFilm.getId(), newFilm.getName());
        if (newFilm.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ValidationException("Id должен быть указан");
        }


        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getDescription() != null) {
                if (newFilm.getDescription().length() > MAX_DESCRIPTION) {
                    log.warn("Попытка обновления фильма с описанием длиной {} символов (максимум {}) (ID: {})",
                            newFilm.getDescription().length(), MAX_DESCRIPTION, newFilm.getId());
                    throw new ValidationException("максимальная длина описания — " + MAX_DESCRIPTION + " символов");
                }
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getName() != null) {
                if (newFilm.getName().isBlank()) {
                    log.warn("Попытка обновления фильма с пустым названием (ID: {})", newFilm.getId());
                    throw new ValidationException("название не может быть пустым");
                }
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getReleaseDate() != null) {
                if (newFilm.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
                    log.warn("Попытка обновления фильма с датой релиза {} раньше {} (ID: {})",
                            newFilm.getReleaseDate(), CINEMA_BIRTHDAY, newFilm.getId());
                    throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
                }
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() > 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            if (newFilm.getDuration() <= 0) {
                log.warn("Попытка обновления фильма {} с некорректной продолжительностью {} (ID: {})",
                        newFilm.getName(), newFilm.getDuration(), newFilm.getId());
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
            log.info("Фильм успешно обновлен. ID: {}, Название: {}",
                    newFilm.getId(), newFilm.getName());
            return oldFilm;
        }
        log.warn("Попытка обновления несуществующего фильма с ID: {}", newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}