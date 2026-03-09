package ru.yandex.practicum.filmorate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest extends CreateUserRequest {
    @NotNull(message = "ID пользователя обязателен")
    private Long id;
}
