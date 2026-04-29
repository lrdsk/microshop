package com.example.microshop.notification_service.service;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import com.example.microshop.notification_service.utils.OrderRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchOrderRecordServiceImpl implements SearchOrderRecordService {
    private final OrderRecordRepository orderRecordRepository;
    private final OrderRecordMapper orderRecordMapper;

    @Override
    public List<OrderRecordResponseDTO> findAll() {
        return orderRecordRepository.findAll()
                .stream()
                .map(orderRecordMapper::mapFromOrderRecordEntityToOrderRecordResponseDTO)
                .toList();
    }

    @Override
    public List<OrderRecordResponseDTO> findByOrderId(UUID orderId) {
        return orderRecordRepository.findByOrderId(orderId)
                .stream()
                .map(orderRecordMapper::mapFromOrderRecordEntityToOrderRecordResponseDTO)
                .toList();
    }

    @Override
    public List<OrderRecordResponseDTO> findByUserId(UUID userId) {
        return orderRecordRepository.findByUserId(userId)
                .stream()
                .map(orderRecordMapper::mapFromOrderRecordEntityToOrderRecordResponseDTO)
                .toList();
    }
}
