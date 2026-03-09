package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGenreStorage implements GenreStorage {

    private final Map<Integer, Genre> genres = new ConcurrentHashMap<>();

    public InMemoryGenreStorage() {
        genres.put(1, new Genre(1, "Комедия"));
        genres.put(2, new Genre(2, "Драма"));
        genres.put(3, new Genre(3, "Мультфильм"));
        genres.put(4, new Genre(4, "Триллер"));
        genres.put(5, new Genre(5, "Документальный"));
        genres.put(6, new Genre(6, "Боевик"));
    }

    @Override
    public Collection<Genre> findAll() {
        return genres.values();
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return Optional.ofNullable(genres.get(id));
    }

    public Genre getById(Integer id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}