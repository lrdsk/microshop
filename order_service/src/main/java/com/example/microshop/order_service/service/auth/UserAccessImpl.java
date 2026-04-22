package com.example.microshop.order_service.service.auth;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.service.UserAccessService;
import com.example.microshop.order_service.service.UserService;
import com.example.microshop.order_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
import com.example.microshop.order_service.service.auth.command.RefreshAccessTokenResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserAccessImpl implements UserAccessService {
    private final JWTUtils jwtUtils;
    private final UserService userService;

    @Override
    public RefreshAccessTokenResult refreshAccessToken(String refreshToken) throws EntityNotFoundException {
        log.info("Called UserAccessService to refresh user access token");

        UUID userId = jwtUtils.extractUserId(refreshToken);
        User user = userService.findUserById(userId);

        String accessToken = jwtUtils.generateAccessToken(userId, user.getUsername(), user.getRole().toString());
        String updatedRefreshToken = jwtUtils.generateRefreshToken(userId);

        return new RefreshAccessTokenResult(accessToken, updatedRefreshToken);
    }

    @Override
    public CreateAccessAndRefreshTokensByUsernameResult createAccessAndRefreshTokensByUsername(String username) throws EntityNotFoundException {
        log.info("Called UserAccessService to create tokens for user with username: {}", username);

        User user = userService.findUserByUsername(username);
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getPassword());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        return new CreateAccessAndRefreshTokensByUsernameResult(accessToken, refreshToken);
    }


}
