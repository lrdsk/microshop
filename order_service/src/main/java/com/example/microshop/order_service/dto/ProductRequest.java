package com.example.microshop.order_service.dto;

import java.util.UUID;

public record ProductRequest(
        UUID productId,
        int quantity
) {
}
