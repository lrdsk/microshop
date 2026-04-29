package com.example.microshop.order_service.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {
    @Getter
    private final UUID orderId;
    @Getter
    private final UUID userId;
    @Getter
    private double totalAmount = 0;
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
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
