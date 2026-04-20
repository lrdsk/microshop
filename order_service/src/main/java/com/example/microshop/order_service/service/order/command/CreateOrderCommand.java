package com.example.microshop.order_service.service.order.command;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateOrderCommand(String username, List<OrderItemValue> orderItemValues) {
    public CreateOrderCommand {
        Objects.requireNonNull(username, "username must be not null in CreateOrderCommand");

        if(username.isBlank()) {
            throw new IllegalStateException("username must be not blank in CreateOrderCommand");
        }
    }
    public record OrderItemValue(UUID productId, Integer quantity, Double price, Integer sale) {
        public OrderItemValue {
            Objects.requireNonNull(productId, "productId must be not null in OrderItemValue");
            Objects.requireNonNull(quantity, "quantity must be not null in OrderItemValue");
            Objects.requireNonNull(price, "price must be not null in OrderItemValue");
            Objects.requireNonNull(sale, "sale must be not null in OrderItemValue");
        }
    }
}
