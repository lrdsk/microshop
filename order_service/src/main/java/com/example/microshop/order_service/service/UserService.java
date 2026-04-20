package com.example.microshop.order_service.service;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.service.user.command.RegisterUserCommand;
import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public interface UserService {
    User findUserByUsername(String username) throws EntityNotFoundException;
    void register(RegisterUserCommand command);
    User findUserById(UUID userId) throws EntityNotFoundException;
}
