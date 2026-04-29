package com.example.microshop.notification_service.kafka.consumer;

import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import com.example.microshop.notification_service.utils.OrderRecordMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kafka-потребитель для обработки событий о созданных заказах.
 * <p>
 * Слушает топик {@code orders}, проверяет внутренний API-ключ,
 * извлекает traceId из заголовков сообщения и сохраняет записи заказов
 * в базу данных сервиса уведомлений.
 * </p>
 *
 * @see OrderCreatedEvent
 * @see OrderRecordRepository
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRecordRepository repository;
    private final OrderRecordMapper orderRecordMapper;
    @Value("${internal.api.key}")
    private String internalApiKey;

    /**
     * Основной метод-обработчик сообщений из топика {@code orders}.
     * <p>
     * Выполняет следующие шаги:
     * <ol>
     *     <li>Проверяет наличие и корректность заголовка {@code X-Internal-Api-Key}.
     *         При несовпадении логгирует ошибку и завершает обработку.</li>
     *     <li>Извлекает traceId из заголовка {@code X-Trace-Id} (или генерирует новый),
     *         помещает его в {@link MDC} для сквозной трассировки логов.</li>
     *     <li>Десериализует тело сообщения в объект {@link OrderCreatedEvent}.</li>
     *     <li>Преобразует событие в список сущностей {@link OrderRecordEntity}
     *         с помощью {@link OrderRecordMapper}.</li>
     *     <li>Сохраняет сущности в репозитории.</li>
     *     <li>В блоке {@code finally} удаляет traceId из MDC.</li>
     * </ol>
     * </p>
     *
     * @param record запись Kafka, содержащая ключ, значение и заголовки
     */
    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(ConsumerRecord<String, String> record) {
        if (validateApiKeyFromRecord(record)) return;

        String traceId = getTraceIdFromRecord(record);

        MDC.put("X-Trace-Id", traceId);
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
            log.info("Received order event: {}", event.getOrderId());

            List<OrderRecordEntity> records = orderRecordMapper.fromOrderCreatedEventToOrderRecordEntities(event);

            repository.saveAll(records);
            log.info("Saved {} order items for orderId: {}", records.size(), event.getOrderId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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