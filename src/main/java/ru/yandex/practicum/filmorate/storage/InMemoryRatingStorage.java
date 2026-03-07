package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRatingStorage implements RatingStorage {

    private final Map<Integer, Rating> ratings = new ConcurrentHashMap<>();

    public InMemoryRatingStorage() {
        ratings.put(1, new Rating(1, "G"));
        ratings.put(2, new Rating(2, "PG"));
        ratings.put(3, new Rating(3, "PG-13"));
        ratings.put(4, new Rating(4, "R"));
        ratings.put(5, new Rating(5, "NC-17"));
    }

    @Override
    public Collection<Rating> findAll() {
        return ratings.values();
    }

    @Override
    public Optional<Rating> findById(Integer id) {
        return Optional.ofNullable(ratings.get(id));
    }
}