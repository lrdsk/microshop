package com.example.microshop.notification_service.controller;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.service.SearchOrderRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для получения информации о заказах в сервисе уведомлений.
 * <p>
 * Предоставляет эндпоинты для:
 * <ul>
 *     <li>получения всех записей заказов;</li>
 *     <li>поиска записей по идентификатору заказа;</li>
 *     <li>поиска записей по идентификатору пользователя.</li>
 * </ul>
 * </p>
 * <p>
 * Базовый путь: {@code /api/orders}.
 * </p>
 *
 * @see SearchOrderRecordService
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderRecordController {
    private final SearchOrderRecordService searchOrderRecordService;

    /**
     * Возвращает список всех записей заказов.
     *
     * @return список {@link OrderRecordResponseDTO}
     */
    @GetMapping("/all")
    public List<OrderRecordResponseDTO> getAllOrderRecords() {
        log.info("REST request to find orders");
        return searchOrderRecordService.findAll();
    }

    /**
     * Возвращает все записи заказов, относящиеся к конкретному заказу.
     *
     * @param orderId идентификатор заказа (UUID)
     * @return список {@link OrderRecordResponseDTO} для указанного заказа
     */
    @GetMapping("/by-order/{orderId}")
    public List<OrderRecordResponseDTO> getAllOrderRecordsByOrderId(@PathVariable("orderId") UUID orderId) {
        return searchOrderRecordService.findByOrderId(orderId);
    }

    /**
     * Возвращает все записи заказов, относящиеся к конкретному пользователю.
     *
     * @param userId идентификатор пользователя (UUID)
     * @return список {@link OrderRecordResponseDTO} для указанного пользователя
     */
    @GetMapping("/by-user/{userId}")
    public List<OrderRecordResponseDTO> getAllOrderRecordsByUserId(@PathVariable("userId") UUID userId) {
        return searchOrderRecordService.findByUserId(userId);
    }
}
