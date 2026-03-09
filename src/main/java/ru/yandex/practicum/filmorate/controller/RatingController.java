package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class RatingController {

    private final RatingDbStorage ratingStorage;

    @Autowired
    public RatingController(RatingDbStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        return ResponseEntity.ok(ratingStorage.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable Integer id) {
        return ResponseEntity.ok(ratingStorage.getById(id));
    }
}