package com.example.microshop.notification_service.kafka.consumer;

import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRecordRepository repository;

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(OrderCreatedEvent event) {
        log.info("Received order event: {}", event.orderId());

        List<OrderRecordEntity> records = event.orderItemEvents().stream()
                .map(item -> new OrderRecordEntity(
                        UUID.randomUUID(),
                        event.orderId(),
                        item.productId(),
                        item.quantity(),
                        new BigDecimal(item.price()),
                        item.sale(),
                        new BigDecimal(item.totalPrice()),
                        event.userId()
                ))
                .collect(Collectors.toList());

        repository.saveAll(records);
        log.info("Saved {} order items for orderId: {}", records.size(), event.orderId());
    }
}