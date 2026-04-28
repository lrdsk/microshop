package com.example.microshop.inventory_service.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты для ProductFactory")
class ProductFactoryTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String NAME = "Laptop";
    private static final Integer QUANTITY = 50;
    private static final Double PRICE = 999.99;
    private static final Integer SALE = 15;

    @Test
    @DisplayName("createProduct создаёт продукт с корректными значениями")
    void shouldCreateProductWithValidValues() {
        Product product = ProductFactory.createProduct(PRODUCT_ID, NAME, QUANTITY, PRICE, SALE);

        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(PRODUCT_ID);
        assertThat(product.getName()).isEqualTo(NAME);
        assertThat(product.getQuantity()).isEqualTo(QUANTITY);
        assertThat(product.getPrice()).isEqualTo(PRICE);
        assertThat(product.getSale()).isEqualTo(SALE);
    }

    @Test
    @DisplayName("createProduct бросает NPE при null productId")
    void throwsExceptionWhenProductIdIsNull() {
        assertThatThrownBy(() -> ProductFactory.createProduct(null, NAME, QUANTITY, PRICE, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("productId to create product must be not null");
    }

    @Test
    @DisplayName("createProduct бросает NPE при null name")
    void throwsExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ProductFactory.createProduct(PRODUCT_ID, null, QUANTITY, PRICE, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name to create product must be not null");
    }

    @Test
    @DisplayName("createProduct бросает NPE при null quantity")
    void throwsExceptionWhenQuantityIsNull() {
        assertThatThrownBy(() -> ProductFactory.createProduct(PRODUCT_ID, NAME, null, PRICE, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("quantity to create product must be not null");
    }

    @Test
    @DisplayName("createProduct бросает NPE при null price")
    void throwsExceptionWhenPriceIsNull() {
        assertThatThrownBy(() -> ProductFactory.createProduct(PRODUCT_ID, NAME, QUANTITY, null, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("price to create product must be not null");
    }

    @Test
    @DisplayName("createProduct бросает NPE при null sale")
    void throwsExceptionWhenSaleIsNull() {
        assertThatThrownBy(() -> ProductFactory.createProduct(PRODUCT_ID, NAME, QUANTITY, PRICE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("sale to create product must be not null");
    }

    @Test
    @DisplayName("setAudit устанавливает временные метки")
    void shouldSetAuditTimestamps() {
        Product product = ProductFactory.createProduct(PRODUCT_ID, NAME, QUANTITY, PRICE, SALE);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime modifiedAt = createdAt.plusHours(1);

        ProductFactory.setAudit(product, createdAt, modifiedAt);

        assertThat(product.getCreatedAt()).isEqualTo(createdAt);
        assertThat(product.getModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    @DisplayName("setAudit корректно обрабатывает null (метод не проверяет аргументы)")
    void setAuditAcceptsNull() {
        Product product = ProductFactory.createProduct(PRODUCT_ID, NAME, QUANTITY, PRICE, SALE);

        ProductFactory.setAudit(product, null, null);

        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getModifiedAt()).isNull();
    }
}
