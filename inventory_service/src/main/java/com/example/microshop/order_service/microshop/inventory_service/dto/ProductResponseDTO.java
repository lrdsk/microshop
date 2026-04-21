package com.example.microshop.order_service.microshop.inventory_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        Integer quantity,
        Double price,
        Integer sale,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {
}
