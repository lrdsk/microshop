package com.example.microshop.auth_service.service.user;

import com.example.microshop.auth_service.domain.User;
import com.example.microshop.auth_service.domain.UserFactory;
import com.example.microshop.auth_service.entity.UserEntity;
import com.example.microshop.auth_service.repository.UserRepository;
import com.example.microshop.auth_service.service.UserService;
import com.example.microshop.auth_service.service.user.command.RegisterUserCommand;
import com.example.microshop.auth_service.utils.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса для управления пользователями.
 * <p>
 * Предоставляет методы для:
 * <ul>
 *     <li>поиска пользователя по имени или идентификатору;</li>
 *     <li>регистрации новых пользователей с кодированием пароля.</li>
 * </ul>
 * </p>
 * <p>
 * Использует {@link UserRepository} для доступа к базе данных,
 * {@link PasswordEncoder} для безопасного хранения паролей и
 * {@link UserMapper} для преобразования между сущностями JPA и доменными объектами.
 * </p>
 *
 * @see UserService
 * @see UserRepository
 * @see PasswordEncoder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Поиск пользователя по имени.
     *
     * @param username имя пользователя (логин)
     * @return доменный объект {@link User}, соответствующий найденной сущности
     * @throws EntityNotFoundException если пользователь с указанным именем не существует
     */
    @Override
    public User findUserByUsername(String username) throws EntityNotFoundException {
        log.info("Called UserService to find user by username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username: '%s' not found".formatted(username)));
        log.info("The user has been found successfully");
        return userMapper.fromEntity(userEntity);
    }

    /**
     * Регистрация нового пользователя.
     * <p>
     * Перед сохранением проверяет, не занято ли указанное имя пользователя.
     * Пароль кодируется с помощью {@link PasswordEncoder} перед сохранением.
     * </p>
     *
     * @param command команда, содержащая имя пользователя, пароль и email
     * @throws IllegalStateException если пользователь с таким именем уже существует
     */
    @Override
    public void register(RegisterUserCommand command) {
        log.info("Called UserService to register new user with username: {}", command.username());
        Optional<UserEntity> userEntity = userRepository.findByUsername(command.username());

        if(userEntity.isPresent()) {
            throw new IllegalStateException("User with username: '%s' already exists".formatted(command.username()));
        }

        User user = UserFactory.createUser(command.username(), passwordEncoder.encode(command.password()), command.email());

        userRepository.save(userMapper.toEntity(user));

        log.info("The user with username: '%s' has been created successfully");
    }

    /**
     * Поиск пользователя по уникальному идентификатору.
     *
     * @param userId идентификатор пользователя (UUID)
     * @return доменный объект {@link User}
     * @throws EntityNotFoundException если пользователь с указанным ID не найден
     */
    @Override
    public User findUserById(UUID userId) throws EntityNotFoundException {
        log.info("Called UserService to find user with userId: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with userId: '%s' not found".formatted(userId)));

        return userMapper.fromEntity(userEntity);
    }
}
