package com.example.microshop.order_service.domain;

import java.util.Objects;
import java.util.UUID;

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
