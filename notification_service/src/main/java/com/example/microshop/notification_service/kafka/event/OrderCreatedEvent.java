package com.example.microshop.notification_service.kafka.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId,
                                UUID userId,
                                List<OrderItemEvent> orderItemEvents,
                                double totalAmount,
                                LocalDateTime createdAt) {
    public record OrderItemEvent(UUID productId,
                                 int quantity,
                                 double price,
                                 int sale,
                                 double totalPrice) {

    }
}
