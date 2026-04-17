package com.example.microshop.order_service.service;

import com.example.microshop.order_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;

public interface UserAccessService {
    String refreshAccessToken(String refreshToken) throws ClassNotFoundException;
    CreateAccessAndRefreshTokensByUsernameResult createAccessAndRefreshTokensByUsername(String username) throws ClassNotFoundException;
}
