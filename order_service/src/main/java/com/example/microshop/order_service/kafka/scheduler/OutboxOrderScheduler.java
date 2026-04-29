package com.example.microshop.order_service.kafka.scheduler;

import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Планировщик для обработки событий outbox (паттерн Transactional Outbox).
 * <p>
 * Регулярно сканирует таблицу outbox на наличие событий со статусом {@code PENDING}
 * и отправляет их в Kafka-топик {@code orders}.
 * Для каждого события добавляются заголовки:
 * <ul>
 *     <li>{@code X-Internal-Api-Key} — для аутентификации на стороне потребителя;</li>
 *     <li>{@code X-Trace-Id} — для сквозного логирования.</li>
 * </ul>
 * После успешной отправки статус события обновляется на {@code SENT}
 * и проставляется время отправки.
 * </p>
 *
 * @see OutboxRepository
 * @see KafkaTemplate
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxOrderScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${internal.api.key}")
    private String internalApiKey;
    private static final String TOPIC = "orders";

    /**
     * Периодически обрабатывает ожидающие отправки события.
     * <p>
     * Выполняется с фиксированной задержкой 5 секунд.
     * Загружает не более 100 событий со статусом PENDING.
     * Для каждого события:
     * <ol>
     *     <li>извлекает traceId (или генерирует новый);</li>
     *     <li>устанавливает traceId в MDC;</li>
     *     <li>формирует производительную запись Kafka с заголовками API-ключа и traceId;</li>
     *     <li>отправляет запись;</li>
     *     <li>обновляет статус события и время отправки;</li>
     *     <li>в случае ошибки логгирует её, событие остаётся в статусе PENDING
     *         для повторной обработки в следующем цикле.</li>
     * </ol>
     * Всегда очищает MDC после обработки каждого события.
     * </p>
     */
    @Scheduled(fixedDelay = 5000)
    public void processPendingEvents() {
        List<OutboxEntity> pendingEvents = outboxRepository.findOutboxEntityByStatusPending(PageRequest.of(0, 100));
        for (OutboxEntity event : pendingEvents) {
            UUID traceId = event.getTraceId();
            if (traceId == null) traceId = UUID.randomUUID();

            MDC.put("X-Trace-Id", traceId.toString());
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, event.getAggregateId().toString(), event.getPayload());
                record.headers().add("X-Internal-Api-Key", internalApiKey.getBytes(StandardCharsets.UTF_8));
                record.headers().add("X-Trace-Id", traceId.toString().getBytes(StandardCharsets.UTF_8));

                kafkaTemplate.send(record);

                event.setStatus(EventStatus.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxRepository.save(event);
                log.info("Outbox event sent: id={}, orderId={}", event.getId(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to send outbox event: id={}", event.getId(), e);
            } finally {
                MDC.remove("X-Trace-Id");
            }
        }
    }
}