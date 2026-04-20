package com.example.microshop.order_service.domain;

import java.util.UUID;

public final class OrderFactory {
    private OrderFactory() {}

    public static Order createOrder(UUID userId) {
        return new Order(UUID.randomUUID(), userId);
    }

    public static OrderItem createOrderItem(UUID productId, Integer quantity, Double price, Integer sale) {
        return new OrderItem(
                UUID.randomUUID(),
                productId,
                quantity,
                price,
                sale
        );
    }
}
