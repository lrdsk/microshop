package com.example.microshop.order_service.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Фабрика для создания доменных объектов {@link Order} и {@link OrderItem}.
 * <p>
 * Предоставляет статические методы для создания заказов и позиций заказа
 * с автоматической генерацией идентификаторов.
 * </p>
 * <p>
 * Класс является финальным и имеет приватный конструктор,
 * запрещающий создание экземпляров (утилитарный класс).
 * </p>
 *
 * @see Order
 * @see OrderItem
 */
public final class OrderFactory {
    private OrderFactory() {}

    /**
     * Создаёт новый заказ для указанного пользователя.
     * <p>
     * Генерирует случайный UUID для заказа и связывает его с идентификатором пользователя.
     * </p>
     *
     * @param userId идентификатор пользователя, создающего заказ (не может быть {@code null})
     * @return новый экземпляр {@link Order} со сгенерированным идентификатором
     * @throws NullPointerException если {@code userId} равен {@code null}
     */
    public static Order createOrder(UUID userId) {
        return new Order(UUID.randomUUID(), Objects.requireNonNull(userId,"userId must not be null to create order"));
    }

    /**
     * Создаёт новую позицию заказа для указанного товара.
     * <p>
     * Генерирует случайный UUID для позиции заказа и связывает его с данными товара.
     * </p>
     *
     * @param productId идентификатор товара (не может быть {@code null})
     * @param quantity  запрашиваемое количество товара (не может быть {@code null})
     * @param price     цена товара (не может быть {@code null})
     * @param sale      скидка на товар (не может быть {@code null})
     * @return новый экземпляр {@link OrderItem} со сгенерированным идентификатором
     * @throws NullPointerException если любой из аргументов равен {@code null}
     */
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
