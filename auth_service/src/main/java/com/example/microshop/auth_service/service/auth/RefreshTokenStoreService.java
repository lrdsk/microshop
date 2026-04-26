package com.example.microshop.auth_service.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenStoreService {

    private final StringRedisTemplate redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    /**
     * Сохраняет refresh-токен с TTL, равным времени жизни токена (в миллисекундах)
     */
    public void store(String token, long ttlMillis) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, "valid", Duration.ofMillis(ttlMillis));
    }

    /**
     * Проверяет, существует ли токен в Redis и не истёк ли он
     */
    public boolean isValid(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Удаляет токен
     */
    public void revoke(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }
}