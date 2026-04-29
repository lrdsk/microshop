    package com.example.microshop.order_service.domain;

    import lombok.Getter;

    import java.util.UUID;

    @Getter
    public class OrderItem {
        private final UUID itemId;
        private final UUID productId;
        private int quantity;
        private double price;
        private double totalPrice;
        private int sale;

        OrderItem(UUID itemId, UUID productId, Integer quantity, Double price, Integer sale) {
            this.itemId = itemId;
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
            this.sale = sale;
            this.totalPrice = price * quantity * ((100 - sale) / 100.0);
        }

    }
