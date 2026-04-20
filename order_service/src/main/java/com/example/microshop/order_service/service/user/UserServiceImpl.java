package com.example.microshop.order_service.service.user;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.domain.UserFactory;
import com.example.microshop.order_service.entity.UserEntity;
import com.example.microshop.order_service.repository.UserRepository;
import com.example.microshop.order_service.service.UserService;
import com.example.microshop.order_service.service.user.command.RegisterUserCommand;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findUserByUsername(String username) throws EntityNotFoundException {
        log.info("Called UserService to find user by username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username: '%s' not found".formatted(username)));
        log.info("The user has been found successfully");
        return userMapper.fromEntity(userEntity);
    }

    @Override
    public void register(RegisterUserCommand command) {
        log.info("Called UserService to register new user with username: {} and email: {}", command.username(), command.email());
        Optional<UserEntity> userEntity = userRepository.findByUsername(command.username());

        if(userEntity.isPresent()) {
            throw new IllegalStateException("User with username: '%s' already exists".formatted(command.username()));
        }

        User user = UserFactory.createUser(command.username(), passwordEncoder.encode(command.password()), command.email());

        userRepository.save(userMapper.toEntity(user));

        log.info("The user with username: '%s' has been created successfully");
    }

    @Override
    public User findUserById(UUID userId) throws EntityNotFoundException {
        log.info("Called UserService to find user with userId: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with userId: '%s' not found".formatted(userId)));

        return userMapper.fromEntity(userEntity);
    }
}
