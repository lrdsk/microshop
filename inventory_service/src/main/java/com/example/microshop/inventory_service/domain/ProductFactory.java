package com.example.microshop.inventory_service.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ProductFactory {
    private ProductFactory() {
    }

    public static Product createProduct(
            UUID productId,
            String name,
            Integer quantity,
            Double price,
            Integer sale
    ) {
        return new Product(productId, name, quantity, price, sale);
    }

    public static void setAudit(Product product, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        product.setCreatedAt(createdAt);
        product.setModifiedAt(modifiedAt);
    }
}
