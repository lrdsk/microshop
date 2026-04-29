package com.example.microshop.auth_service.domain;


import java.util.Objects;
import java.util.UUID;

/**
 * Фабрика для создания экземпляров {@link User}.
 * <p>
 * Предоставляет статические методы для создания пользователей,
 * гарантируя, что все обязательные поля (id, username, password, email)
 * не являются {@code null}. В случае передачи {@code null} выбрасывается
 * {@link NullPointerException} с информативным сообщением.
 * </p>
 * <p>
 * Класс является финальным и имеет приватный конструктор,
 * запрещающий создание экземпляров (утилитарный класс).
 * </p>
 * @see User
 */
public final class UserFactory {
    private UserFactory() {}

    public static User createUser(String username, String password, String email) {
        return new User(
                UUID.randomUUID(),
                Objects.requireNonNull(username, "Username to create user must be not null"),
                Objects.requireNonNull(email, "Email to create user must be not null"),
                Objects.requireNonNull(password, "Password to create user must be not null")
        );
    }

    public static User createUser(UUID id, String username, String password, String email) {
        return new User(
                Objects.requireNonNull(id, "Id to create user must be not null"),
                Objects.requireNonNull(username, "Username to create user must be not null"),
                Objects.requireNonNull(email, "Email to create user must be not null"),
                Objects.requireNonNull(password, "Password to create user must be not null")
        );
    }
}
