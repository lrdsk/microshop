package com.example.microshop.auth_service.domain;

import com.example.microshop.auth_service.domain.User;
import com.example.microshop.auth_service.domain.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты для UserFactory")
class UserFactoryTest {

    @Test
    @DisplayName("Создание пользователя с автоматической генерацией UUID")
    void shouldCreateUserWithGeneratedId() {
        //given
        String username = "testuser";
        String password = "password123";
        String email = "test@example.com";

        //when
        User user = UserFactory.createUser(username, password, email);

        //then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Создание пользователя с заданным UUID")
    void shouldCreateUserWithProvidedId() {
        //given
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String username = "anotheruser";
        String password = "pass456";
        String email = "another@example.com";

        //when
        User user = UserFactory.createUser(id, username, password, email);

        //then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Выброс исключения при создании пользователя с null username")
    void shouldThrowExceptionWhenUsernameIsNull() {
        //when then
        assertThatThrownBy(() -> UserFactory.createUser(null, "password", "email@test.com"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Username to create user must be not null");
    }

    @Test
    @DisplayName("Выброс исключения при создании пользователя с null password")
    void shouldThrowExceptionWhenPasswordIsNull() {
        //when then
        assertThatThrownBy(() -> UserFactory.createUser("username", null, "email@test.com"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Password to create user must be not null");
    }

    @Test
    @DisplayName("Выброс исключения при создании пользователя с null email")
    void shouldThrowExceptionWhenEmailIsNull() {
        //when then
        assertThatThrownBy(() -> UserFactory.createUser("username", "password", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Email to create user must be not null");
    }

    @Test
    @DisplayName("Выброс исключения при передаче null в createUser(UUID, String, String, String)")
    void shouldThrowExceptionWhenAnyArgumentIsNullInFullConstructor() {
        //given
        UUID id = UUID.fromString("83edaf02-5035-4d82-87ae-e05f822bb5a1");

        //when then
        assertThatThrownBy(() -> UserFactory.createUser(null, "user", "pass", "mail"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Id to create user must be not null");

        assertThatThrownBy(() -> UserFactory.createUser(id, null, "pass", "mail"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Username to create user must be not null");

        assertThatThrownBy(() -> UserFactory.createUser(id, "user", null, "mail"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Password to create user must be not null");

        assertThatThrownBy(() -> UserFactory.createUser(id, "user", "pass", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Email to create user must be not null");
    }
}
