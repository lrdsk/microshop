package com.example.microshop.notification_service.service;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import com.example.microshop.notification_service.utils.OrderRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для поиска записей заказов в сервисе уведомлений.
 * <p>
 * Предоставляет методы для получения списка записей заказов
 * с фильтрацией по различным критериям (все, по идентификатору заказа,
 * по идентификатору пользователя).
 * </p>
 *
 * @see SearchOrderRecordService
 * @see OrderRecordRepository
 * @see OrderRecordMapper
 */
@Service
@RequiredArgsConstructor
public class SearchOrderRecordServiceImpl implements SearchOrderRecordService {
    private final OrderRecordRepository orderRecordRepository;
    private final OrderRecordMapper orderRecordMapper;

    /**
     * Возвращает все записи заказов, преобразованные в DTO.
     *
     * @return список {@link OrderRecordResponseDTO}
     */
    @Override
    public List<OrderRecordResponseDTO> findAll() {
        return orderRecordRepository.findAll()
                .stream()
                .map(orderRecordMapper::mapFromOrderRecordEntityToOrderRecordResponseDTO)
                .toList();
    }

    /**
     * Возвращает записи заказов, относящиеся к конкретному заказу.
     *
     * @param orderId идентификатор заказа (UUID)
     * @return список DTO для указанного заказа
     */
    @Override
    public List<OrderRecordResponseDTO> findByOrderId(UUID orderId) {
        return orderRecordRepository.findByOrderId(orderId)
                .stream()
                .map(orderRecordMapper::mapFromOrderRecordEntityToOrderRecordResponseDTO)
                .toList();
    }

    /**
     * Возвращает записи заказов, относящиеся к конкретному пользователю.
     *
     * @param userId идентификатор пользователя (UUID)
     * @return список DTO для указанного пользователя
     */
    @Override
    public List<OrderRecordResponseDTO> findByUserId(UUID userId) {
        return orderRecordRepository.findByUserId(userId)
                .stream()
                .map(orderRecordMapper::mapFromOrderRecordEntityToOrderRecordResponseDTO)
                .toList();
    }
}
