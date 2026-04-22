package com.example.microshop.order_service.service.auth.command;

public record RefreshAccessTokenResult(String accessToken, String refreshToken) {
}
