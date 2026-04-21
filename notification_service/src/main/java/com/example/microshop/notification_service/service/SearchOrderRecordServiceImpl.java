package com.example.microshop.notification_service.service;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchOrderRecordServiceImpl implements SearchOrderRecordService {
    private final OrderRecordRepository orderRecordRepository;

    @Override
    public List<OrderRecordResponseDTO> findAll() {
        return orderRecordRepository.findAll()
                .stream()
                .map(SearchOrderRecordServiceImpl::mapToOrderRecordResponseDTO)
                .toList();
    }

    @Override
    public List<OrderRecordResponseDTO> findByOrderId(UUID orderId) {
        return orderRecordRepository.findByOrderId(orderId)
                .stream()
                .map(SearchOrderRecordServiceImpl::mapToOrderRecordResponseDTO)
                .toList();
    }

    @Override
    public List<OrderRecordResponseDTO> findByUserId(UUID userId) {
        return orderRecordRepository.findByUserId(userId)
                .stream()
                .map(SearchOrderRecordServiceImpl::mapToOrderRecordResponseDTO)
                .toList();
    }

    private static OrderRecordResponseDTO mapToOrderRecordResponseDTO(OrderRecordEntity orderRecordEntity) {
        return new OrderRecordResponseDTO(
                orderRecordEntity.getId(),
                orderRecordEntity.getOrderId(),
                orderRecordEntity.getProductId(),
                orderRecordEntity.getQuantity(),
                orderRecordEntity.getPrice().doubleValue(),
                orderRecordEntity.getSale(),
                orderRecordEntity.getTotalPrice().doubleValue(),
                orderRecordEntity.getUserId()
        );
    }
}
