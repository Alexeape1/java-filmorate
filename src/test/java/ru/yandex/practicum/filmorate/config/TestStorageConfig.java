package ru.yandex.practicum.filmorate.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.*;

@TestConfiguration
@Import({FilmDbStorage.class, GenreDbStorage.class, RatingDbStorage.class, UserDbStorage.class})
public class TestStorageConfig {

}