package com.example.microshop.order_service.dto;

import java.util.UUID;

public record OrderRequest(
        UUID productId,
        int quantity
) {
}
