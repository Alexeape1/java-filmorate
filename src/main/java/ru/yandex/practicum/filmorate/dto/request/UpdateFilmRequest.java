package ru.yandex.practicum.filmorate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateFilmRequest extends CreateFilmRequest {
    @NotNull(message = "ID фильма обязателен")
    private Long id;
}
