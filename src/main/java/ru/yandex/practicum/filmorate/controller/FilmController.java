package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> findAll() {
        Collection<Film> films = filmService.findAll();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Film> findById(@PathVariable long filmId) {
        Film findId = filmService.findById(filmId);
        return ResponseEntity.ok(findId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Film> create(@RequestBody Film film) {
        Film createFilm = filmService.create(film);
        return ResponseEntity.ok(createFilm);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film newFilm) {
        Film updateFilm = filmService.update(newFilm);
        return ResponseEntity.ok(updateFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopularFilms(
            @RequestParam(defaultValue = "10") @Min(1) int count) {
        Collection<Film> popularFilms = filmService.getPopularFilms(count);
        return ResponseEntity.ok(popularFilms);
    }
}