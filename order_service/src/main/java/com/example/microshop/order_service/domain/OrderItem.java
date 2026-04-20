package com.example.microshop.order_service.domain;

import lombok.Getter;

import java.util.UUID;

public class OrderItem {
    @Getter
    private final UUID itemId;
    @Getter
    private final UUID productId;
    @Getter
    private Integer quantity;
    @Getter
    private Double price;
    @Getter
    private Integer sale;

    OrderItem(UUID itemId, UUID productId, Integer quantity, Double price, Integer sale) {
        this.itemId = itemId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.sale = sale;
    }
}
