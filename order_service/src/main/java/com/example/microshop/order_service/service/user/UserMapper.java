package com.example.microshop.order_service.service.user;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User fromEntity(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getPassword());
    }
}
