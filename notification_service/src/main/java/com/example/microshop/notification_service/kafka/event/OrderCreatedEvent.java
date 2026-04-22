package com.example.microshop.notification_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderCreatedEvent {
    private UUID orderId;
    private UUID userId;
    private List<OrderItemEvent> orderItemEvents;
    private double totalAmount;
    private LocalDateTime createdAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class OrderItemEvent {
        private UUID productId;
        private int quantity;
        private double price;
        private int sale;
        private double totalPrice;
    }
}
