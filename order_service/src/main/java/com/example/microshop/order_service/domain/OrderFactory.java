package com.example.microshop.order_service.domain;

import java.util.Objects;
import java.util.UUID;

public final class OrderFactory {
    private OrderFactory() {}

    public static Order createOrder(UUID userId) {
        return new Order(UUID.randomUUID(), Objects.requireNonNull(userId,"userId must not be null to create order"));
    }

    public static OrderItem createOrderItem(UUID productId, Integer quantity, Double price, Integer sale) {
        return new OrderItem(
                UUID.randomUUID(),
                Objects.requireNonNull(productId,"userId must not be null to create order"),
                Objects.requireNonNull(quantity,"quantity must not be null to create order"),
                Objects.requireNonNull(price,"price must not be null to create order"),
                Objects.requireNonNull(sale,"sale must not be null to create order")
        );
    }
}
