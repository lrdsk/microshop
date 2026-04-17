package com.example.microshop.order_service.controller;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.dto.AuthRequestDTO;
import com.example.microshop.order_service.dto.AuthResponseDTO;
import com.example.microshop.order_service.dto.RegisterRequestDTO;
import com.example.microshop.order_service.service.UserService;
import com.example.microshop.order_service.service.auth.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request) throws ClassNotFoundException {
        log.info("Try to login with username: {}", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        log.info("user with username {} has been successfully authenticated", request.username());
        User user = userService.findUserByUsername(request.username());
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getPassword());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    @PostMapping("/reg")
    public ResponseEntity<HttpStatus> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Try to register new user with username: {}", registerRequestDTO.username());
        userService.register(registerRequestDTO);
        log.info("The new user has been successfully registered");

        return ResponseEntity.ok(HttpStatus.CREATED);
    }
}
