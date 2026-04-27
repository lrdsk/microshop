package com.example.microshop.notification_service.kafka.consumer;

import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRecordRepository repository;
    @Value("${internal.api.key}")
    private String internalApiKey;

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(ConsumerRecord<String, String> record) {
        if (validateApiKeyFromRecord(record)) return;

        String traceId = getTraceIdFromRecord(record);

        MDC.put("X-Trace-Id", traceId);
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
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
        } finally {
            MDC.remove("X-Trace-Id");
        }
    }

    private boolean validateApiKeyFromRecord(ConsumerRecord<String, String> record) {
        Header apiKeyHeader = record.headers().lastHeader("X-Internal-Api-Key");
        if (apiKeyHeader == null || !internalApiKey.equals(new String(apiKeyHeader.value(), StandardCharsets.UTF_8))) {
            log.error("Invalid or missing API Key. Message rejected.");
            return true;
        }
        return false;
    }

    private static String getTraceIdFromRecord(ConsumerRecord<String, String> record) {
        String traceId = null;
        Header header = record.headers().lastHeader("X-Trace-Id");
        if (header != null) {
            traceId = new String(header.value(), StandardCharsets.UTF_8);
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}