package com.example.microshop.order_service.service.user;

import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.domain.UserFactory;
import com.example.microshop.order_service.entity.Role;
import com.example.microshop.order_service.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User fromEntity(UserEntity userEntity) {
        return UserFactory.createUser(userEntity.getId(), userEntity.getUsername(), userEntity.getPassword(), userEntity.getEmail());
    }

    public UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.getId());
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setEmail(user.getEmail());
        userEntity.setRole(Role.valueOf(user.getRole().toString()));

        return userEntity;
    }
}
