package com.example.microshop.order_service.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты для OrderFactory")
class OrderFactoryTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final Integer QUANTITY = 5;
    private static final Double PRICE = 199.99;
    private static final Integer SALE = 15;

    @Test
    @DisplayName("createOrder создаёт заказ с корректным userId и генерирует id")
    void shouldCreateOrderWithValidUserId() {
        Order order = OrderFactory.createOrder(USER_ID);
        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isNotNull();
        assertThat(order.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("createOrder выбрасывает NullPointerException при null userId")
    void shouldThrowNullPointerExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> OrderFactory.createOrder(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId must not be null to create order");
    }

    @Test
    @DisplayName("createOrderItem создаёт позицию заказа с корректными параметрами")
    void shouldCreateOrderItemWithValidParameters() {
        OrderItem item = OrderFactory.createOrderItem(PRODUCT_ID, QUANTITY, PRICE, SALE);
        assertThat(item).isNotNull();
        assertThat(item.getItemId()).isNotNull();
        assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(item.getQuantity()).isEqualTo(QUANTITY);
        assertThat(item.getPrice()).isEqualTo(PRICE);
        assertThat(item.getSale()).isEqualTo(SALE);
    }

    @Test
    @DisplayName("createOrderItem выбрасывает NullPointerException при null productId")
    void shouldThrowExceptionWhenProductIdIsNull() {
        assertThatThrownBy(() -> OrderFactory.createOrderItem(null, QUANTITY, PRICE, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId must not be null to create order");
    }

    @Test
    @DisplayName("createOrderItem выбрасывает NullPointerException при null quantity")
    void shouldThrowExceptionWhenQuantityIsNull() {
        assertThatThrownBy(() -> OrderFactory.createOrderItem(PRODUCT_ID, null, PRICE, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("quantity must not be null to create order");
    }

    @Test
    @DisplayName("createOrderItem выбрасывает NullPointerException при null price")
    void shouldThrowExceptionWhenPriceIsNull() {
        assertThatThrownBy(() -> OrderFactory.createOrderItem(PRODUCT_ID, QUANTITY, null, SALE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("price must not be null to create order");
    }

    @Test
    @DisplayName("createOrderItem выбрасывает NullPointerException при null sale")
    void shouldThrowExceptionWhenSaleIsNull() {
        assertThatThrownBy(() -> OrderFactory.createOrderItem(PRODUCT_ID, QUANTITY, PRICE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("sale must not be null to create order");
    }
}
