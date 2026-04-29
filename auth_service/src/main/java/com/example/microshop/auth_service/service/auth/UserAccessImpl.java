package com.example.microshop.auth_service.service.auth;

import com.example.microshop.auth_service.domain.User;
import com.example.microshop.auth_service.service.UserAccessService;
import com.example.microshop.auth_service.service.UserService;
import com.example.microshop.auth_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
import com.example.microshop.auth_service.service.auth.command.RefreshAccessTokenResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Реализация сервиса для управления доступом пользователя,
 * включая генерацию access и refresh токенов.
 * <p>
 * Использует {@link JWTUtils} для создания и обработки JWT-токенов,
 * а также {@link UserService} для получения данных пользователя.
 * </p>
 *
 * @see UserAccessService
 * @see JWTUtils
 * @see UserService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserAccessImpl implements UserAccessService {
    private final JWTUtils jwtUtils;
    private final UserService userService;

    /**
     * Обновляет access токен на основе действующего refresh токена.
     * <p>
     * Извлекает userId из refresh токена, загружает пользователя,
     * генерирует новый access токен и обновлённый refresh токен.
     * </p>
     *
     * @param refreshToken действующий refresh токен
     * @return результат операции, содержащий новый access токен и обновлённый refresh токен
     * @throws EntityNotFoundException если пользователь с идентификатором из токена не найден
     */
    @Override
    public RefreshAccessTokenResult refreshAccessToken(String refreshToken) throws EntityNotFoundException {
        log.info("Called UserAccessService to refresh user access token");

        UUID userId = jwtUtils.extractUserId(refreshToken);
        User user = userService.findUserById(userId);

        String accessToken = jwtUtils.generateAccessToken(userId, user.getUsername(), user.getRole().toString());
        String updatedRefreshToken = jwtUtils.generateRefreshToken(userId);

        return new RefreshAccessTokenResult(accessToken, updatedRefreshToken);
    }

    /**
     * Создаёт пару access и refresh токенов для пользователя по его имени.
     * <p>
     * Находит пользователя по имени, затем генерирует для него
     * access токен (используя id, username и пароль) и refresh токен (только id).
     * </p>
     *
     * @param username имя пользователя
     * @return результат, содержащий access и refresh токены
     * @throws EntityNotFoundException если пользователь с указанным именем не найден
     */
    @Override
    public CreateAccessAndRefreshTokensByUsernameResult createAccessAndRefreshTokensByUsername(String username) throws EntityNotFoundException {
        log.info("Called UserAccessService to create tokens for user with username: {}", username);

        User user = userService.findUserByUsername(username);
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getPassword());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        return new CreateAccessAndRefreshTokensByUsernameResult(accessToken, refreshToken);
    }
}
