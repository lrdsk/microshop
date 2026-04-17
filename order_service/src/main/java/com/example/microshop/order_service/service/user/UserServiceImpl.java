package com.example.microshop.order_service.service.user;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.entity.UserEntity;
import com.example.microshop.order_service.repository.UserRepository;
import com.example.microshop.order_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User findUserByUsername(String username) throws ClassNotFoundException {
        log.info("Called API to find user by username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClassNotFoundException("User with username: '%s' not found".formatted(username)));

        return userMapper.fromEntity(userEntity);
    }
}
