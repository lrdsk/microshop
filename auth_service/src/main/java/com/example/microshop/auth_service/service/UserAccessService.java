package com.example.microshop.auth_service.service;


import com.example.microshop.auth_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
import com.example.microshop.auth_service.service.auth.command.RefreshAccessTokenResult;
import jakarta.persistence.EntityNotFoundException;

public interface UserAccessService {
    RefreshAccessTokenResult refreshAccessToken(String refreshToken) throws EntityNotFoundException;
    CreateAccessAndRefreshTokensByUsernameResult createAccessAndRefreshTokensByUsername(String username) throws EntityNotFoundException;
}
