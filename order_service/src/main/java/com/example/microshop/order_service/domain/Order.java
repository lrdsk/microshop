package com.example.microshop.order_service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    private final UUID orderId;
    private final UUID userId;
    private double totalAmount;
    private List<OrderItem> items = new ArrayList<>();

    Order(UUID orderId, UUID userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public void addItem(OrderItem item) {
        items.stream()
                .filter(currentItem -> item.getItemId().equals(currentItem.getItemId()))
                .findFirst()
                .ifPresent(currentItem -> {
                    throw new IllegalStateException("This item already exists in current order, item id: %s".formatted(currentItem.getItemId()));
                });

        items.add(item);
    }
}
