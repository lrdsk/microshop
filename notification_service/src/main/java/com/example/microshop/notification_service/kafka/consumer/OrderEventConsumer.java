package com.example.microshop.notification_service.kafka.consumer;

import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRecordRepository repository;

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(String jsonMessage) {
        OrderCreatedEvent event = objectMapper.readValue(jsonMessage, OrderCreatedEvent.class);
        log.info("Received order event: {}", event.getOrderId());

        List<OrderRecordEntity> records = event.getOrderItemEvents().stream()
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

        repository.saveAll(records);
        log.info("Saved {} order items for orderId: {}", records.size(), event.getOrderId());
    }
}