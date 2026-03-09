package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
            if (user != null) {
                user.setFriends(getFriends(id));
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, user.getEmail());
        if (count != null && count > 0) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName() == null || user.getName().isBlank() ? user.getLogin() : user.getName());
        parameters.put("birthday", user.getBirthday());

        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(id.longValue());

        log.info("Создан пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        String checkExistsSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer exists = jdbcTemplate.queryForObject(checkExistsSql, Integer.class, user.getId());
        if (exists == null || exists == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, user.getEmail(), user.getId());
        if (count != null && count > 0) {
            throw new ValidationException("Email " + user.getEmail() + " уже используется другим пользователем");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName() == null || user.getName().isBlank() ? user.getLogin() : user.getName(),
                user.getBirthday(),
                user.getId());

        if (updated == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        log.info("Обновлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);

        if (deleted == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        log.info("Удален пользователь с id: {}", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        validateUserExists(userId);
        validateUserExists(friendId);

        String sql = "MERGE INTO user_friends (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        validateUserExists(userId);

        String sql = "SELECT friend_id FROM user_friends WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId);
        validateUserExists(otherId);

        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "   SELECT uf1.friend_id FROM user_friends uf1 " +
                "   WHERE uf1.user_id = ? " +
                "   INTERSECT " +
                "   SELECT uf2.friend_id FROM user_friends uf2 " +
                "   WHERE uf2.user_id = ?" +
                ")";

        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    private void validateUserExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}