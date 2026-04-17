package com.example.microshop.order_service.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank
        String username,
        @NotBlank
        String password) {
}
