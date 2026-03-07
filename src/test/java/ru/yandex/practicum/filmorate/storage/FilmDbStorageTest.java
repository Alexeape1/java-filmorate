package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.config.TestStorageConfig;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(TestStorageConfig.class)
@Sql(statements = {
        "DELETE FROM film_genres;",
        "DELETE FROM film_likes;",
        "DELETE FROM films;",
        "DELETE FROM users;",
        "ALTER TABLE films ALTER COLUMN id RESTART WITH 1;",
        "ALTER TABLE users ALTER COLUMN id RESTART WITH 1;",

        "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES " +
                "('Film 1', 'Description 1', '2000-01-01', 120, 1);",
        "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES " +
                "('Film 2', 'Description 2', '2001-02-02', 90, 2);",
        "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES " +
                "('Film 3', 'Description 3', '2002-03-03', 150, 3);",

        "INSERT INTO users (email, login, name, birthday) VALUES " +
                "('user1@test.com', 'user1', 'User One', '1990-01-01');",
        "INSERT INTO users (email, login, name, birthday) VALUES " +
                "('user2@test.com', 'user2', 'User Two', '1991-02-02');",
        "INSERT INTO users (email, login, name, birthday) VALUES " +
                "('user3@test.com', 'user3', 'User Three', '1992-03-03');",

        "INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1);",
        "INSERT INTO film_genres (film_id, genre_id) VALUES (1, 2);",
        "INSERT INTO film_genres (film_id, genre_id) VALUES (2, 3);",

        "INSERT INTO film_likes (film_id, user_id) VALUES (1, 1);",
        "INSERT INTO film_likes (film_id, user_id) VALUES (1, 2);",
        "INSERT INTO film_likes (film_id, user_id) VALUES (2, 1);"
})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final RatingDbStorage ratingStorage;
    private final UserDbStorage userStorage;

    @Test
    public void testFindFilmById_WhenFilmExists_ShouldReturnFilm() {
        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Film 1");
                    assertThat(film).hasFieldOrPropertyWithValue("description", "Description 1");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 120);
                    assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));

                    assertThat(film.getMpa()).isNotNull();
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                    assertThat(film.getMpa().getName()).isEqualTo("G");

                    assertThat(film.getGenres()).hasSize(2);
                    assertThat(film.getGenres()).extracting("id")
                            .containsExactlyInAnyOrder(1, 2);
                    assertThat(film.getGenres()).extracting("name")
                            .containsExactlyInAnyOrder("Комедия", "Драма");

                    assertThat(film.getLikes()).hasSize(2);
                    assertThat(film.getLikes()).containsExactlyInAnyOrder(1L, 2L);
                });
    }

    @Test
    public void testFindFilmById_WhenFilmNotExists_ShouldReturnEmpty() {
        Optional<Film> filmOptional = filmStorage.findById(999L);

        assertThat(filmOptional).isEmpty();
    }

    @Test
    public void testFindAll_ShouldReturnAllFilms() {
        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);

        Film film1 = films.stream().filter(f -> f.getId() == 1L).findFirst().orElse(null);
        Film film2 = films.stream().filter(f -> f.getId() == 2L).findFirst().orElse(null);
        Film film3 = films.stream().filter(f -> f.getId() == 3L).findFirst().orElse(null);

        assertThat(film1).isNotNull();
        assertThat(film1.getGenres()).hasSize(2);

        assertThat(film2).isNotNull();
        assertThat(film2.getGenres()).hasSize(1);
        assertThat(film2.getGenres().iterator().next().getId()).isEqualTo(3);

        assertThat(film3).isNotNull();
        assertThat(film3.getGenres()).isEmpty();
    }

    @Test
    public void testCreateFilm_ShouldSaveAndReturnFilmWithId() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        newFilm.setDuration(180);

        Optional<Rating> rating = ratingStorage.findById(4);
        rating.ifPresent(newFilm::setMpa);

        Set<Genre> genres = new HashSet<>();
        genreStorage.findById(1).ifPresent(genres::add);
        genreStorage.findById(6).ifPresent(genres::add);
        newFilm.setGenres(genres);

        Film created = filmStorage.create(newFilm);

        assertThat(created.getId()).isEqualTo(4L);

        Optional<Film> saved = filmStorage.findById(4L);
        assertThat(saved)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("name", "New Film");
                    assertThat(film).hasFieldOrPropertyWithValue("description", "New Description");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 180);

                    assertThat(film.getMpa()).isNotNull();
                    assertThat(film.getMpa().getId()).isEqualTo(4);
                    assertThat(film.getMpa().getName()).isEqualTo("R");

                    assertThat(film.getGenres()).hasSize(2);
                    assertThat(film.getGenres()).extracting("id")
                            .containsExactlyInAnyOrder(1, 6);
                });
    }

    @Test
    public void testUpdateFilm_ShouldUpdateExistingFilm() {
        Optional<Film> filmOptional = filmStorage.findById(1L);
        assertThat(filmOptional).isPresent();

        Film film = filmOptional.get();
        film.setName("Updated Film 1");
        film.setDescription("Updated Description");
        film.setDuration(200);

        Optional<Rating> newRating = ratingStorage.findById(5);
        newRating.ifPresent(film::setMpa);

        Set<Genre> newGenres = new HashSet<>();
        genreStorage.findById(4).ifPresent(newGenres::add);
        genreStorage.findById(5).ifPresent(newGenres::add);
        film.setGenres(newGenres);

        Film updated = filmStorage.update(film);

        assertThat(updated).hasFieldOrPropertyWithValue("name", "Updated Film 1");
        assertThat(updated).hasFieldOrPropertyWithValue("description", "Updated Description");
        assertThat(updated).hasFieldOrPropertyWithValue("duration", 200);

        Optional<Film> saved = filmStorage.findById(1L);
        assertThat(saved)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Updated Film 1");
                    assertThat(f.getMpa().getId()).isEqualTo(5);
                    assertThat(f.getGenres()).hasSize(2);
                    assertThat(f.getGenres()).extracting("id")
                            .containsExactlyInAnyOrder(4, 5);
                });
    }

    @Test
    public void testUpdateFilm_WhenFilmNotExists_ShouldThrowException() {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Non Existent");
        nonExistentFilm.setDescription("Description");
        nonExistentFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        nonExistentFilm.setDuration(120);

        assertThatThrownBy(() -> filmStorage.update(nonExistentFilm))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    public void testDeleteFilm_ShouldRemoveFilm() {
        Optional<Film> beforeDelete = filmStorage.findById(1L);
        assertThat(beforeDelete).isPresent();

        filmStorage.delete(1L);

        Optional<Film> afterDelete = filmStorage.findById(1L);
        assertThat(afterDelete).isEmpty();

        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(2);
    }

    @Test
    public void testDeleteFilm_WhenFilmNotExists_ShouldThrowException() {
        assertThatThrownBy(() -> filmStorage.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    public void testAddLike_ShouldAddLikeToFilm() {
        Optional<Film> beforeLike = filmStorage.findById(3L);
        assertThat(beforeLike).isPresent();
        assertThat(beforeLike.get().getLikes()).isEmpty();

        filmStorage.addLike(3L, 3L);

        Optional<Film> afterLike = filmStorage.findById(3L);
        assertThat(afterLike).isPresent();
        assertThat(afterLike.get().getLikes()).hasSize(1);
        assertThat(afterLike.get().getLikes()).contains(3L);
    }

    @Test
    public void testAddLike_WhenAlreadyLiked_ShouldNotDuplicate() {
        Optional<Film> beforeLike = filmStorage.findById(1L);
        assertThat(beforeLike).isPresent();
        assertThat(beforeLike.get().getLikes()).hasSize(2);

        filmStorage.addLike(1L, 1L);

        Optional<Film> afterLike = filmStorage.findById(1L);
        assertThat(afterLike).isPresent();
        assertThat(afterLike.get().getLikes()).hasSize(2);
    }

    @Test
    public void testRemoveLike_ShouldRemoveLikeFromFilm() {
        Optional<Film> beforeRemove = filmStorage.findById(1L);
        assertThat(beforeRemove).isPresent();
        assertThat(beforeRemove.get().getLikes()).hasSize(2);

        filmStorage.removeLike(1L, 1L);

        Optional<Film> afterRemove = filmStorage.findById(1L);
        assertThat(afterRemove).isPresent();
        assertThat(afterRemove.get().getLikes()).hasSize(1);
        assertThat(afterRemove.get().getLikes()).contains(2L);
        assertThat(afterRemove.get().getLikes()).doesNotContain(1L);
    }

    @Test
    public void testRemoveLike_WhenLikeNotExists_ShouldDoNothing() {
        Optional<Film> beforeRemove = filmStorage.findById(3L);
        assertThat(beforeRemove).isPresent();
        assertThat(beforeRemove.get().getLikes()).isEmpty();

        filmStorage.removeLike(3L, 1L);

        Optional<Film> afterRemove = filmStorage.findById(3L);
        assertThat(afterRemove).isPresent();
        assertThat(afterRemove.get().getLikes()).isEmpty();
    }

    @Test
    public void testGetPopularFilms_ShouldReturnFilmsSortedByLikes() {
        Collection<Film> popularFilms = filmStorage.getPopularFilms(3);

        assertThat(popularFilms).hasSize(3);

        Film[] films = popularFilms.toArray(new Film[0]);
        assertThat(films[0].getId()).isEqualTo(1L);
        assertThat(films[1].getId()).isEqualTo(2L);
        assertThat(films[2].getId()).isEqualTo(3L);
    }

    @Test
    public void testGetPopularFilms_WithLimit_ShouldReturnLimitedResults() {
        Collection<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms).extracting("id")
                .containsExactly(1L, 2L);
    }

    @Test
    public void testGetPopularFilms_WithGenres_ShouldLoadGenresCorrectly() {
        Collection<Film> popularFilms = filmStorage.getPopularFilms(2);

        Film film1 = popularFilms.stream().filter(f -> f.getId() == 1L).findFirst().orElse(null);
        assertThat(film1).isNotNull();
        assertThat(film1.getGenres()).hasSize(2);

        Film film2 = popularFilms.stream().filter(f -> f.getId() == 2L).findFirst().orElse(null);
        assertThat(film2).isNotNull();
        assertThat(film2.getGenres()).hasSize(1);
    }

    @Test
    public void testFilmWithMultipleGenres_ShouldSaveAndLoadAllGenres() {
        Film newFilm = new Film();
        newFilm.setName("Multi-Genre Film");
        newFilm.setDescription("Film with many genres");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(120);

        Optional<Rating> rating = ratingStorage.findById(3);
        rating.ifPresent(newFilm::setMpa);

        Set<Genre> genres = new HashSet<>();
        genreStorage.findById(1).ifPresent(genres::add);
        genreStorage.findById(2).ifPresent(genres::add);
        genreStorage.findById(4).ifPresent(genres::add);
        genreStorage.findById(6).ifPresent(genres::add);
        newFilm.setGenres(genres);

        Film created = filmStorage.create(newFilm);

        Optional<Film> saved = filmStorage.findById(created.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getGenres()).hasSize(4);
        assertThat(saved.get().getGenres()).extracting("id")
                .containsExactlyInAnyOrder(1, 2, 4, 6);
    }

    @Test
    public void testUpdateFilm_ShouldUpdateGenresCorrectly() {
        Optional<Film> filmOptional = filmStorage.findById(2L);
        assertThat(filmOptional).isPresent();

        Film film = filmOptional.get();
        assertThat(film.getGenres()).hasSize(1);
        assertThat(film.getGenres().iterator().next().getId()).isEqualTo(3);

        Set<Genre> newGenres = new HashSet<>();
        genreStorage.findById(1).ifPresent(newGenres::add);
        genreStorage.findById(2).ifPresent(newGenres::add);
        film.setGenres(newGenres);

        filmStorage.update(film);

        Optional<Film> updated = filmStorage.findById(2L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getGenres()).hasSize(2);
        assertThat(updated.get().getGenres()).extracting("id")
                .containsExactlyInAnyOrder(1, 2);
    }
}