package com.example.microshop.auth_service.service.user.command;

public record RegisterUserCommand(String username,
                                  String password,
                                  String email) {
}
