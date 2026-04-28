package com.example.microshop.auth_service.service.user;

import com.example.microshop.auth_service.domain.User;
import com.example.microshop.auth_service.domain.UserFactory;
import com.example.microshop.auth_service.entity.UserEntity;
import com.example.microshop.auth_service.repository.UserRepository;
import com.example.microshop.auth_service.service.user.command.RegisterUserCommand;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // Тестовые данные
    private static final UUID EXPECTED_USER_ID = UUID.fromString("f5ab8eeb-869a-4229-8583-14ed4023feec");
    private static final String EXPECTED_USERNAME = "test_name";
    private static final String EXPECTED_PASSWORD_RAW = "secret123";
    private static final String EXPECTED_PASSWORD_ENCODED = "$2a$10$encodedHash";
    private static final String EXPECTED_EMAIL = "test@example.com";

    @Test
    @DisplayName("findUserByUsername: успешный поиск по имени")
    void shouldFindUserByUsername() {
        //given
        UserEntity entity = createTestUserEntity();
        User expectedUser = createTestUser();

        when(userRepository.findByUsername(EXPECTED_USERNAME)).thenReturn(Optional.of(entity));
        when(userMapper.fromEntity(entity)).thenReturn(expectedUser);

        //when
        User result = userService.findUserByUsername(EXPECTED_USERNAME);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(EXPECTED_USER_ID);
        assertThat(result.getUsername()).isEqualTo(EXPECTED_USERNAME);
        assertThat(result.getEmail()).isEqualTo(EXPECTED_EMAIL);
        verify(userRepository).findByUsername(EXPECTED_USERNAME);
        verify(userMapper).fromEntity(entity);
    }

    @Test
    @DisplayName("findUserByUsername: пользователь не найден -> EntityNotFoundException")
    void shouldThrowNotFoundWhenUsernameMissing() {
        //given
        when(userRepository.findByUsername(EXPECTED_USERNAME)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> userService.findUserByUsername(EXPECTED_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(EXPECTED_USERNAME);
        verify(userRepository).findByUsername(EXPECTED_USERNAME);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("findUserById: успешный поиск по ID")
    void shouldFindUserById() {
        //given
        UserEntity entity = createTestUserEntity();
        User expectedUser = createTestUser();

        when(userRepository.findById(EXPECTED_USER_ID)).thenReturn(Optional.of(entity));
        when(userMapper.fromEntity(entity)).thenReturn(expectedUser);

        //when
        User result = userService.findUserById(EXPECTED_USER_ID);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(EXPECTED_USER_ID);
        verify(userRepository).findById(EXPECTED_USER_ID);
        verify(userMapper).fromEntity(entity);
    }

    @Test
    @DisplayName("findUserById: пользователь с таким ID не найден -> EntityNotFoundException")
    void shouldThrowNotFoundWhenIdMissing() {
        //given
        when(userRepository.findById(EXPECTED_USER_ID)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> userService.findUserById(EXPECTED_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(EXPECTED_USER_ID.toString());
        verify(userRepository).findById(EXPECTED_USER_ID);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("register: успешная регистрация нового пользователя")
    void shouldRegisterNewUser() {
        //given
        RegisterUserCommand command = new RegisterUserCommand(EXPECTED_USERNAME, EXPECTED_PASSWORD_RAW, EXPECTED_EMAIL);
        UserEntity entity = new UserEntity();
        when(userRepository.findByUsername(EXPECTED_USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(EXPECTED_PASSWORD_RAW)).thenReturn(EXPECTED_PASSWORD_ENCODED);
        when(userMapper.toEntity(any(User.class))).thenReturn(entity);

        //when
        userService.register(command);

        //then
        verify(userRepository).findByUsername(EXPECTED_USERNAME);
        verify(passwordEncoder).encode(EXPECTED_PASSWORD_RAW);
        verify(userMapper).toEntity(any(User.class));
        verify(userRepository).save(entity);
    }

    @Test
    @DisplayName("register: попытка регистрации с уже существующим username -> IllegalStateException")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        //given
        RegisterUserCommand command = new RegisterUserCommand(EXPECTED_USERNAME, EXPECTED_PASSWORD_RAW, EXPECTED_EMAIL);
        UserEntity existingEntity = createTestUserEntity();

        when(userRepository.findByUsername(EXPECTED_USERNAME)).thenReturn(Optional.of(existingEntity));

        //when then
        assertThatThrownBy(() -> userService.register(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(EXPECTED_USERNAME);
        verify(userRepository).findByUsername(EXPECTED_USERNAME);
        verifyNoInteractions(passwordEncoder, userMapper);
        verify(userRepository, never()).save(any());
    }

    private static @NotNull UserEntity createTestUserEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(EXPECTED_USER_ID);
        entity.setUsername(EXPECTED_USERNAME);
        entity.setPassword(EXPECTED_PASSWORD_ENCODED);
        entity.setEmail(EXPECTED_EMAIL);

        return entity;
    }

    private static @NotNull User createTestUser() {
        return UserFactory.createUser(EXPECTED_USER_ID, EXPECTED_USERNAME, EXPECTED_PASSWORD_ENCODED, EXPECTED_EMAIL);
    }
}
