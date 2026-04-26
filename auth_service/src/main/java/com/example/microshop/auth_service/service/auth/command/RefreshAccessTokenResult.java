package com.example.microshop.auth_service.service.auth.command;

public record RefreshAccessTokenResult(String accessToken, String refreshToken) {
}
