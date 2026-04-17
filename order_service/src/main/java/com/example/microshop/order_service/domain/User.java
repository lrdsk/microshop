package com.example.microshop.order_service.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public User(UUID id, String username, String email, String password) {
        this.id = Objects.requireNonNull(id, "id for user must be not null");
        this.username = Objects.requireNonNull(username, "username for user must be not null");
        this.email = Objects.requireNonNull(email, "email for user must be not null");
        this.password = Objects.requireNonNull(password, "password for user must be not null");
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
