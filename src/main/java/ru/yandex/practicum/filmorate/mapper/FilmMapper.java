package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.dto.request.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    public FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikesCount(film.getLikes() != null ? film.getLikes().size() : 0);

        if (film.getMpa() != null) {
            RatingDto mpaDto = new RatingDto();
            mpaDto.setId(film.getMpa().getId());
            mpaDto.setName(film.getMpa().getName());
            dto.setMpa(mpaDto);
        } else {
            RatingDto defaultMpa = new RatingDto();
            defaultMpa.setId(1);
            defaultMpa.setName("G");
            dto.setMpa(defaultMpa);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<GenreDto> genreDtos = film.getGenres().stream()
                    .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                    .sorted(Comparator.comparing(GenreDto::getId))
                    .collect(Collectors.toList());
            dto.setGenres(genreDtos);
        } else {
            dto.setGenres(new ArrayList<>());
        }
        return dto;
    }

    public Film toEntity(CreateFilmRequest request) {
        if (request == null) {
            return null;
        }

        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        return film;
    }

    public Film updateEntity(Film film, UpdateFilmRequest request) {
        if (request == null) {
            return film;
        }

        if (request.getName() != null) {
            film.setName(request.getName());
        }
        if (request.getDescription() != null) {
            film.setDescription(request.getDescription());
        }
        if (request.getReleaseDate() != null) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.getDuration() > 0) {
            film.setDuration(request.getDuration());
        }
        if (request.getMpaId() != null) {
            Rating rating = new Rating();
            rating.setId(request.getMpaId());
            film.setMpa(rating);
        }

        return film;
    }

    public Set<Genre> mapGenreIdsToGenres(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Collections.emptySet();
        }
        return genreIds.stream()
                .map(id -> {
                    Genre genre = new Genre();
                    genre.setId(id);
                    return genre;
                })
                .collect(Collectors.toSet());
    }
}