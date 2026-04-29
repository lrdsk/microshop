package com.example.microshop.auth_service.controller;

import com.example.microshop.auth_service.dto.*;
import com.example.microshop.auth_service.service.UserAccessService;
import com.example.microshop.auth_service.service.UserService;
import com.example.microshop.auth_service.service.auth.JWTUtils;
import com.example.microshop.auth_service.service.auth.RefreshTokenStoreService;
import com.example.microshop.auth_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
import com.example.microshop.auth_service.service.auth.command.RefreshAccessTokenResult;
import com.example.microshop.auth_service.service.user.command.RegisterUserCommand;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для аутентификации и управления пользователями.
 * <p>
 * Предоставляет эндпоинты для:
 * <ul>
 *     <li>входа в систему (логин) — выдача access и refresh токенов;</li>
 *     <li>регистрации нового пользователя;</li>
 *     <li>обновления access токена с использованием refresh токена.</li>
 * </ul>
 * </p>
 * <p>
 * Все эндпоинты доступны по базовому пути {@code /auth}.
 * </p>
 *
 * @see AuthRequestDTO
 * @see AuthResponseDTO
 * @see RegisterRequestDTO
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final UserService userService;
    private final UserAccessService userAccessService;
    private final RefreshTokenStoreService refreshTokenStore;

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request) throws EntityNotFoundException {
        log.info("Try to login with username: {}", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        log.info("user with username {} has been successfully authenticated", request.username());
        CreateAccessAndRefreshTokensByUsernameResult tokens = userAccessService.createAccessAndRefreshTokensByUsername(request.username());

        long refreshTtl = jwtUtils.getRefreshExpirationMs();
        refreshTokenStore.store(tokens.refreshToken(), refreshTtl);

        return new AuthResponseDTO(tokens.accessToken(), tokens.refreshToken());
    }

    @PostMapping("/reg")
    public ResponseEntity<HttpStatus> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Try to register new user with username: {}", registerRequestDTO.username());
        userService.register(mapToRegisterUserCommand(registerRequestDTO));
        log.info("The new user has been successfully registered");

        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) throws EntityNotFoundException {
        String refreshToken = refreshTokenRequestDTO.refreshToken();
        log.info("Try to refresh access token with refresh token: {}", refreshToken);

        if (!refreshTokenStore.isValid(refreshToken) || !jwtUtils.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        refreshTokenStore.revoke(refreshToken);
        RefreshAccessTokenResult refreshAccessTokenResult = userAccessService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(new RefreshTokenResponseDTO(refreshAccessTokenResult.accessToken(), refreshAccessTokenResult.refreshToken()));
    }

    private static @NotNull RegisterUserCommand mapToRegisterUserCommand(RegisterRequestDTO registerRequestDTO) {
        return new RegisterUserCommand(registerRequestDTO.username(), registerRequestDTO.password(), registerRequestDTO.email());
    }
}
