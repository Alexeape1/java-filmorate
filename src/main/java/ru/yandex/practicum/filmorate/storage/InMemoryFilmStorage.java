package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    private final Map<Long, Set<Long>> likes = new HashMap<>();

    private long currentId = 1L;

    @Override
    public Collection<Film> findAll() {
        log.info("Текущее количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        boolean nameExists = films.values().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(film.getName()));
        if (nameExists) {
            throw new ValidationException("Фильм с названием '" + film.getName() + "' уже существует");
        }

        film.setId(currentId++);
        films.put(film.getId(), film);
        likes.put(film.getId(), new HashSet<>());
        log.info("Создан фильм с id: {}, название: {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Обновлен фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public void delete(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        films.remove(id);
        likes.remove(id);
        log.info("Удален фильм с id: {}", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        validateFilmExists(filmId);

        Set<Long> filmLikes = likes.computeIfAbsent(filmId, k -> new HashSet<>());

        if (filmLikes.contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " уже поставил лайк фильму " + filmId);
        }

        filmLikes.add(userId);
        Film film = films.get(filmId);
        film.getLikes().add(userId);

        log.info("Добавлен лайк фильму {} от пользователя {}", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        if (likes.containsKey(filmId)) {
            likes.get(filmId).remove(userId);
            Film film = films.get(filmId);
            film.getLikes().remove(userId);
            log.info("Удален лайк у фильма {} от пользователя {}", filmId, userId);
        }
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        log.info("Запрошено {} популярных фильмов", count);

        return films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), new HashSet<>()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), new HashSet<>()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilmExists(Long filmId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }
}