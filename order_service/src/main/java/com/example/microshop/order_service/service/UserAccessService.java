package com.example.microshop.order_service.service;

import com.example.microshop.order_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
import jakarta.persistence.EntityNotFoundException;

public interface UserAccessService {
    String refreshAccessToken(String refreshToken) throws EntityNotFoundException;
    CreateAccessAndRefreshTokensByUsernameResult createAccessAndRefreshTokensByUsername(String username) throws EntityNotFoundException;
}
