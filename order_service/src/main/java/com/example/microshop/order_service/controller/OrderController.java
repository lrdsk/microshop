package com.example.microshop.order_service.controller;

import com.example.microshop.order_service.dto.ProductRequest;
import com.example.microshop.order_service.service.CreatorOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для управления заказами.
 * <p>
 * Предоставляет эндпоинт для создания нового заказа.
 * </p>
 * <p>
 * Базовый путь: {@code /api/order}.
 * </p>
 *
 * @see CreatorOrderService
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final CreatorOrderService createOrderService;

    /**
     * Создаёт новый заказ на основе списка запрашиваемых товаров.
     * <p>
     * Принимает список позиций заказа и идентификатор пользователя из заголовка {@code X-User-Id}.
     * Делегирует создание заказа сервису {@link CreatorOrderService#findProductsAndCreateOrder}.
     * При успешном создании возвращает сообщение с подтверждением.
     * </p>
     *
     * @param productsRequest список позиций заказа (каждый содержит идентификатор товара и количество)
     * @param userId          идентификатор пользователя, полученный из заголовка {@code X-User-Id} (в формате строки)
     * @return ответ со статусом {@code 200 OK} и текстом "Order has been created"
     */
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody List<ProductRequest> productsRequest,
                                              @RequestHeader("X-User-Id") String userId) {

        createOrderService.findProductsAndCreateOrder(productsRequest, UUID.fromString(userId));
        return ResponseEntity.ok("Order has been created");
    }
}
