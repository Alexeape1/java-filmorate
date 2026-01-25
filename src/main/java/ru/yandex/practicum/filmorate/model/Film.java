package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Film {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
}