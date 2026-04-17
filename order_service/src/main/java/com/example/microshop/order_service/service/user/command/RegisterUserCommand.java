package com.example.microshop.order_service.service.user.command;

public record RegisterUserCommand(String username,
                                  String password,
                                  String email) {
}
