package com.example.microshop.order_service.kafka.scheduler;

import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxOrderScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "orders";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingEvents() {
        List<OutboxEntity> pendingEvents = outboxRepository.findOutboxEntityByStatusPending(PageRequest.of(0, 100));
        for (OutboxEntity event : pendingEvents) {
            UUID traceId = event.getTraceId();
            if (traceId == null) traceId = UUID.randomUUID();

            MDC.put("X-Trace-Id", traceId.toString());
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, event.getAggregateId().toString(), event.getPayload());
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