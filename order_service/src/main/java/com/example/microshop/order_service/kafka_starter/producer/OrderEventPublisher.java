package com.example.microshop.order_service.kafka_starter.producer;

import com.example.microshop.order_service.kafka_starter.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "orders";

    public void publishOrderCreated(OrderCreatedEvent event) {
        CompletableFuture<SendResult<String, OrderCreatedEvent>> future =
                kafkaTemplate.send(TOPIC, event.orderId().toString(), event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event sent: {}", event.orderId());
            } else {
                log.error("Failed to send order event: {}", event.orderId(), ex);
            }
        });
    }
}