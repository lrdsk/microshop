package com.example.microshop.order_service.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {
    @Getter
    private final UUID id;
    @Getter
    private String username;
    @Getter
    private String email;
    @Getter
    private String password;
    @Getter
    private Role role = Role.USER;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    User(UUID id, String username, String email, String password) {
        this.id = Objects.requireNonNull(id, "id for user must be not null");
        this.username = Objects.requireNonNull(username, "username for user must be not null");
        this.email = Objects.requireNonNull(email, "email for user must be not null");
        this.password = Objects.requireNonNull(password, "password for user must be not null");
    }
}
