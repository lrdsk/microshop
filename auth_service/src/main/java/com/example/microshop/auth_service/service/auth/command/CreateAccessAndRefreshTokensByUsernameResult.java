package com.example.microshop.auth_service.service.auth.command;

public record CreateAccessAndRefreshTokensByUsernameResult(String accessToken, String refreshToken) {
}
