package com.example.microshop.notification_service.service;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SearchOrderRecordService {
    List<OrderRecordResponseDTO> findAll();
    List<OrderRecordResponseDTO> findByOrderId(UUID orderId);
    List<OrderRecordResponseDTO> findByUserId(UUID userId);
}
