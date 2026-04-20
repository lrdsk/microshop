package com.example.microshop.order_service.kafka_starter.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId,
                                UUID userId,
                                UUID productId,
                                int quantity,
                                double price,
                                int sale,
                                double totalPrice,
                                LocalDateTime createdAt) {
}
