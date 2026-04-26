package com.example.microshop.gateway_service.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JWTUtils {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration.ms}")
    private long ACCESS_EXPIRATION_MS; // 15 минут = 900000

    @Value("${jwt.refresh.expiration.ms}")
    private long REFRESH_EXPIRATION_MS; // 7 дней = 604800000

    public boolean isTokenValid(String refreshToken) {
        try {
            extractAllClaims(refreshToken);
            return !isTokenExpired(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(this.SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
