package com.example.microshop.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(min = 3, max = 255) String username,
        @NotBlank @Size(min = 6, max = 255) String password) {
}
