package com.example.microshop.notification_service.dto;

import java.util.UUID;

public record OrderRecordResponseDTO(
        UUID id,
        UUID orderId,
        UUID productId,
        Integer quantity,
        Double price,
        Integer sale,
        Double totalPrice,
        UUID userId) {
}
