package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.config.TestStorageConfig;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(TestStorageConfig.class)
@Sql(statements = {
        "DELETE FROM user_friends;",
        "DELETE FROM users;",
        "ALTER TABLE users ALTER COLUMN id RESTART WITH 1;",
        "INSERT INTO users (email, login, name, birthday) VALUES " +
                "('user1@test.com', 'user1', 'User One', '1990-01-01');",
        "INSERT INTO users (email, login, name, birthday) VALUES " +
                "('user2@test.com', 'user2', 'User Two', '1991-02-02');",
        "INSERT INTO users (email, login, name, birthday) VALUES " +
                "('user3@test.com', 'user3', 'User Three', '1992-03-03');"
})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById_WhenUserExists_ShouldReturnUser() {
        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(user).hasFieldOrPropertyWithValue("email", "user1@test.com");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "user1");
                    assertThat(user).hasFieldOrPropertyWithValue("name", "User One");
                    assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    public void testFindUserById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> userOptional = userStorage.findById(999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    public void testFindAll_ShouldReturnAllUsers() {
        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(3);
        assertThat(users).extracting("email")
                .containsExactlyInAnyOrder("user1@test.com", "user2@test.com", "user3@test.com");
    }

    @Test
    public void testCreateUser_ShouldSaveAndReturnUserWithId() {
        User newUser = new User();
        newUser.setEmail("newuser@test.com");
        newUser.setLogin("newlogin");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1995, 5, 5));

        User created = userStorage.create(newUser);

        assertThat(created.getId()).isEqualTo(4L);
        assertThat(created).hasFieldOrPropertyWithValue("email", "newuser@test.com");
        assertThat(created).hasFieldOrPropertyWithValue("login", "newlogin");
        assertThat(created).hasFieldOrPropertyWithValue("name", "New User");

        Optional<User> saved = userStorage.findById(4L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    public void testCreateUser_WithDuplicateEmail_ShouldThrowException() {
        User duplicateUser = new User();
        duplicateUser.setEmail("user1@test.com");
        duplicateUser.setLogin("duplicate");
        duplicateUser.setName("Duplicate User");
        duplicateUser.setBirthday(LocalDate.of(1995, 5, 5));

        assertThatThrownBy(() -> userStorage.create(duplicateUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    public void testUpdateUser_ShouldUpdateExistingUser() {
        Optional<User> userOptional = userStorage.findById(1L);
        assertThat(userOptional).isPresent();

        User user = userOptional.get();
        user.setName("Updated Name");
        user.setEmail("updated@test.com");
        user.setLogin("updatedlogin");

        User updated = userStorage.update(user);

        assertThat(updated).hasFieldOrPropertyWithValue("name", "Updated Name");
        assertThat(updated).hasFieldOrPropertyWithValue("email", "updated@test.com");
        assertThat(updated).hasFieldOrPropertyWithValue("login", "updatedlogin");

        Optional<User> saved = userStorage.findById(1L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Updated Name");
        assertThat(saved.get().getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    public void testUpdateUser_WithDuplicateEmail_ShouldThrowException() {
        Optional<User> userOptional = userStorage.findById(1L);
        assertThat(userOptional).isPresent();

        User user = userOptional.get();
        user.setEmail("user2@test.com");

        assertThatThrownBy(() -> userStorage.update(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже используется");
    }

    @Test
    public void testDeleteUser_ShouldRemoveUser() {
        Optional<User> beforeDelete = userStorage.findById(1L);
        assertThat(beforeDelete).isPresent();

        userStorage.delete(1L);

        Optional<User> afterDelete = userStorage.findById(1L);
        assertThat(afterDelete).isEmpty();

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    public void testAddFriend_ShouldAddFriend() {
        userStorage.addFriend(1L, 2L);

        Set<Long> friends = userStorage.getFriends(1L);
        assertThat(friends).contains(2L);
        assertThat(friends).hasSize(1);

        Set<Long> friend2Friends = userStorage.getFriends(2L);
        assertThat(friend2Friends).doesNotContain(1L);
        assertThat(friend2Friends).isEmpty();
    }

    @Test
    public void testAddFriend_WhenAlreadyFriends_ShouldWork() {
        userStorage.addFriend(1L, 2L);

        userStorage.addFriend(1L, 2L);

        Set<Long> friends = userStorage.getFriends(1L);
        assertThat(friends).contains(2L);
        assertThat(friends).hasSize(1);
    }

    @Test
    public void testAddFriend_ToYourself_ShouldThrowException() {
        assertThatThrownBy(() -> userStorage.addFriend(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("самого себя");
    }

    @Test
    public void testRemoveFriend_ShouldRemoveFriend() {
        userStorage.addFriend(1L, 2L);

        Set<Long> beforeRemove = userStorage.getFriends(1L);
        assertThat(beforeRemove).contains(2L);

        userStorage.removeFriend(1L, 2L);

        Set<Long> afterRemove = userStorage.getFriends(1L);
        assertThat(afterRemove).doesNotContain(2L);
        assertThat(afterRemove).isEmpty();
    }

    @Test
    public void testGetFriends_ShouldReturnFriendsList() {
        userStorage.addFriend(1L, 2L);
        userStorage.addFriend(1L, 3L);

        Set<Long> friends = userStorage.getFriends(1L);

        assertThat(friends).hasSize(2);
        assertThat(friends).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    public void testGetFriends_WhenNoFriends_ShouldReturnEmptySet() {
        Set<Long> friends = userStorage.getFriends(1L);

        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetCommonFriends_ShouldReturnCommonFriends() {
        userStorage.addFriend(1L, 2L);
        userStorage.addFriend(1L, 3L);

        User user4 = new User();
        user4.setEmail("user4@test.com");
        user4.setLogin("user4");
        user4.setName("User Four");
        user4.setBirthday(LocalDate.of(1994, 4, 4));
        User created = userStorage.create(user4); // id = 4

        userStorage.addFriend(4L, 3L);
        userStorage.addFriend(4L, 2L);

        Collection<User> commonFriends = userStorage.getCommonFriends(1L, 4L);

        assertThat(commonFriends).hasSize(2);
        assertThat(commonFriends).extracting("id")
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(commonFriends).extracting("login")
                .containsExactlyInAnyOrder("user2", "user3");
    }

    @Test
    public void testGetCommonFriends_WhenNoCommonFriends_ShouldReturnEmptyList() {
        userStorage.addFriend(1L, 2L);

        Collection<User> commonFriends = userStorage.getCommonFriends(1L, 3L);

        assertThat(commonFriends).isEmpty();
    }
}