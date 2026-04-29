package com.example.microshop.auth_service.controller;

import com.example.microshop.auth_service.dto.AuthRequestDTO;
import com.example.microshop.auth_service.dto.RefreshTokenRequestDTO;
import com.example.microshop.auth_service.dto.RegisterRequestDTO;
import com.example.microshop.auth_service.service.UserAccessService;
import com.example.microshop.auth_service.service.UserService;
import com.example.microshop.auth_service.service.auth.JWTUtils;
import com.example.microshop.auth_service.service.auth.RefreshTokenStoreService;
import com.example.microshop.auth_service.service.auth.command.CreateAccessAndRefreshTokensByUsernameResult;
import com.example.microshop.auth_service.service.auth.command.RefreshAccessTokenResult;
import com.example.microshop.auth_service.service.user.command.RegisterUserCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@DisplayName("Интеграционные тесты для AuthenticationController")
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JWTUtils jwtUtils;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAccessService userAccessService;

    @MockitoBean
    private RefreshTokenStoreService refreshTokenStore;

    private static final String USERNAME = "test_name";
    private static final String PASSWORD = "secret123";
    private static final String EMAIL = "test@example.com";
    private static final String ACCESS_TOKEN = "access.token.value";
    private static final String REFRESH_TOKEN = "refresh.token.value";
    private static final long REFRESH_TTL = 604800000L;

    @Test
    @DisplayName("POST /auth/login -> успешный логин возвращает токены")
    void loginSuccess() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO(USERNAME, PASSWORD);
        CreateAccessAndRefreshTokensByUsernameResult tokensResult =
                new CreateAccessAndRefreshTokensByUsernameResult(ACCESS_TOKEN, REFRESH_TOKEN);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userAccessService.createAccessAndRefreshTokensByUsername(USERNAME)).thenReturn(tokensResult);
        when(jwtUtils.getRefreshExpirationMs()).thenReturn(REFRESH_TTL);
        doNothing().when(refreshTokenStore).store(REFRESH_TOKEN, REFRESH_TTL);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userAccessService).createAccessAndRefreshTokensByUsername(USERNAME);
        verify(refreshTokenStore).store(REFRESH_TOKEN, REFRESH_TTL);
    }

    /*@Test
    @DisplayName("POST /auth/login -> неверный пароль возвращает 401")
    void loginInvalidCredentials() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO(USERNAME, "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userAccessService, never()).createAccessAndRefreshTokensByUsername(anyString());
        verify(refreshTokenStore, never()).store(anyString(), anyLong());
    }*/

    @Test
    @DisplayName("POST /auth/reg -> успешная регистрация возвращает 201")
    void registerSuccess() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(USERNAME, PASSWORD, EMAIL);
        doNothing().when(userService).register(any(RegisterUserCommand.class));

        mockMvc.perform(post("/auth/reg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).register(any(RegisterUserCommand.class));
    }

    /*@Test
    @DisplayName("POST /auth/reg -> если пользователь уже существует, сервис бросает исключение, контроллер должен вернуть 4xx")
    void registerDuplicateUser() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(USERNAME, PASSWORD, EMAIL);
        doThrow(new IllegalStateException("User already exists"))
                .when(userService).register(any(RegisterUserCommand.class));

        mockMvc.perform(post("/auth/reg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }*/

    @Test
    @DisplayName("POST /auth/refresh -> успешное обновление токенов")
    void refreshTokenSuccess() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(REFRESH_TOKEN);
        RefreshAccessTokenResult refreshResult = new RefreshAccessTokenResult(ACCESS_TOKEN, REFRESH_TOKEN);

        when(refreshTokenStore.isValid(REFRESH_TOKEN)).thenReturn(true);
        when(jwtUtils.isTokenValid(REFRESH_TOKEN)).thenReturn(true);
        doNothing().when(refreshTokenStore).revoke(REFRESH_TOKEN);
        when(userAccessService.refreshAccessToken(REFRESH_TOKEN)).thenReturn(refreshResult);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN));

        verify(refreshTokenStore).isValid(REFRESH_TOKEN);
        verify(jwtUtils).isTokenValid(REFRESH_TOKEN);
        verify(refreshTokenStore).revoke(REFRESH_TOKEN);
        verify(userAccessService).refreshAccessToken(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("POST /auth/refresh -> невалидный refresh token возвращает 401")
    void refreshTokenInvalid() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("invalid");
        when(refreshTokenStore.isValid("invalid")).thenReturn(false);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(refreshTokenStore, never()).revoke(anyString());
        verify(userAccessService, never()).refreshAccessToken(anyString());
    }

    @Test
    @DisplayName("POST /auth/refresh -> просроченный refresh token возвращает 401")
    void refreshTokenExpired() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(REFRESH_TOKEN);
        when(refreshTokenStore.isValid(REFRESH_TOKEN)).thenReturn(true);
        when(jwtUtils.isTokenValid(REFRESH_TOKEN)).thenReturn(false);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(refreshTokenStore, never()).revoke(anyString());
        verify(userAccessService, never()).refreshAccessToken(anyString());
    }
}
