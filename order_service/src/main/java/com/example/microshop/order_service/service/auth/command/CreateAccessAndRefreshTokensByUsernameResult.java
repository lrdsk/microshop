package com.example.microshop.order_service.service.auth.command;

public record CreateAccessAndRefreshTokensByUsernameResult(String accessToken, String refreshToken) {
}
