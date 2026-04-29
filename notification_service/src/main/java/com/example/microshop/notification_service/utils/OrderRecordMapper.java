package com.example.microshop.notification_service.utils;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.kafka.event.OrderCreatedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderRecordMapper {
    public List<OrderRecordEntity> fromOrderCreatedEventToOrderRecordEntities(OrderCreatedEvent event) {
        return event.getOrderItemEvents().stream()
                .map(item -> new OrderRecordEntity(
                        UUID.randomUUID(),
                        event.getOrderId(),
                        item.getProductId(),
                        item.getQuantity(),
                        new BigDecimal(item.getPrice()),
                        item.getSale(),
                        new BigDecimal(item.getTotalPrice()),
                        event.getUserId()
                ))
                .collect(Collectors.toList());
    }

    public OrderRecordResponseDTO mapFromOrderRecordEntityToOrderRecordResponseDTO(OrderRecordEntity orderRecordEntity) {
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
