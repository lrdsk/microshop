package com.example.microshop.order_service.utils;

import com.example.microshop.order_service.entity.AggregateType;
import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OrderEntity;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.kafka.event.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxBuilder {
    private final ObjectMapper objectMapper;

    public OutboxEntity buildOutboxEventFromOrderEntity(OrderEntity orderEntity) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderEntity.getId(),
                orderEntity.getUserId(),
                orderEntity.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemEvent(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice().doubleValue(),
                                item.getSale(),
                                item.getTotalPrice().doubleValue()
                        )).toList(),
                orderEntity.getTotalAmount().doubleValue(),
                LocalDateTime.now()
        );

        try {
            String currentTraceId = MDC.get("X-Trace-Id");
            String payload = objectMapper.writeValueAsString(event);

            return new OutboxEntity(
                    AggregateType.ORDER,
                    orderEntity.getId(),
                    "OrderCreated",
                    payload,
                    UUID.fromString(currentTraceId),
                    EventStatus.PENDING);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
