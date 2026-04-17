package com.example.microshop.order_service.service;

import com.example.microshop.order_service.domain.User;

public interface UserService {
    User findUserByUsername(String username) throws ClassNotFoundException;
    User register();
}
