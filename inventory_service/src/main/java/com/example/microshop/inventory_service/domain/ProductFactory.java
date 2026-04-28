package com.example.microshop.inventory_service.domain;

import java.time.LocalDateTime;
import java.util.Objects;
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
        return new Product(
                Objects.requireNonNull(productId, "productId to create product must be not null"),
                Objects.requireNonNull(name, "name to create product must be not null"),
                Objects.requireNonNull(quantity, "quantity to create product must be not null"),
                Objects.requireNonNull(price, "price to create product must be not null"),
                Objects.requireNonNull(sale, "sale to create product must be not null"));
    }

    public static void setAudit(Product product, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        product.setCreatedAt(createdAt);
        product.setModifiedAt(modifiedAt);
    }
}
