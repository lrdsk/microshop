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
