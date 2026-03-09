package ru.yandex.practicum.filmorate.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.RatingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CreateFilmRequest {
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    @JsonProperty("mpa")
    private RatingDto mpa;

    @JsonProperty("genres")
    private List<GenreDto> genres;

    public Integer getMpaId() {
        return mpa != null ? mpa.getId() : null;
    }

    public List<Integer> getGenreIds() {
        if (genres == null) {
            return null;
        }
        return genres.stream()
                .map(GenreDto::getId)
                .collect(Collectors.toList());
    }
}