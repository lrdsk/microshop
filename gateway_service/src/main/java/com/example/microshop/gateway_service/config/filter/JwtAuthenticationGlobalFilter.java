package com.example.microshop.gateway_service.config.filter;

import com.example.microshop.gateway_service.service.auth.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Глобальный фильтр для проверки JWT-токенов в API Gateway.
 * <p>
 * Фильтр перехватывает все входящие запросы и выполняет:
 * <ul>
 *     <li>Пропускает публичные пути (начинаются с {@code /auth/} или {@code /actuator/}) без проверки токена.</li>
 *     <li>Для защищённых путей проверяет наличие заголовка {@code Authorization: Bearer <token>}.</li>
 *     <li>Валидирует JWT-токен с помощью {@link JWTUtils#isTokenValid(String)}.</li>
 *     <li>Извлекает из токена userId, username и role.</li>
 *     <li>Добавляет извлечённые данные в заголовки запроса:
 *         {@code X-User-Id}, {@code X-Username}, {@code X-User-Role}.</li>
 *     <li>При отсутствии или недействительности токена возвращает ответ {@code 401 Unauthorized}
 *         с JSON-сообщением об ошибке.</li>
 * </ul>
 * </p>
 * <p>
 * Фильтр имеет приоритет {@code -100} (высокий), что гарантирует его выполнение до маршрутизации.
 * </p>
 *
 * @see GlobalFilter
 * @see JWTUtils
 */
@Component
@Slf4j
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final JWTUtils jwtUtils;
    private static final List<String> PUBLIC_PATHS = List.of("/auth/", "/actuator/");

    public JwtAuthenticationGlobalFilter(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.isTokenValid(token)) {
            return unauthorized(exchange, "Token expired or invalid");
        }

        UUID userId = jwtUtils.extractUserId(token);
        String username = jwtUtils.extractUsername(token);
        String role = jwtUtils.extractRole(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header("X-User-Id", userId.toString())
                               .header("X-Username", username)
                               .header("X-User-Role", role)
                               .build())
                .build();

        return chain.filter(mutatedExchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = String.format("{\"error\":\"%s\"}", message).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
