package com.example.microshop.order_service.service.auth;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.service.UserAccessService;
import com.example.microshop.order_service.service.UserService;
import com.example.microshop.order_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
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
    public String refreshAccessToken(String refreshToken) throws ClassNotFoundException {
        log.info("Called UserAccessService to refresh user access token");

        UUID userId = jwtUtils.extractUserId(refreshToken);
        User user = userService.findUserById(userId);

        return jwtUtils.generateAccessToken(userId, user.getUsername(), user.getRole().toString());
    }

    @Override
    public CreateAccessAndRefreshTokensByUsernameResult createAccessAndRefreshTokensByUsername(String username) throws ClassNotFoundException {
        log.info("Called UserAccessService to create tokens for user with username: {}", username);

        User user = userService.findUserByUsername(username);
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getPassword());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        return new CreateAccessAndRefreshTokensByUsernameResult(accessToken, refreshToken);
    }


}
