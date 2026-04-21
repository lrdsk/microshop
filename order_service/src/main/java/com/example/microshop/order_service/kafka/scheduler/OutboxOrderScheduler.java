package com.example.microshop.order_service.kafka.scheduler;

import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);

                event.setStatus(EventStatus.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxRepository.save(event);
                log.info("Outbox event sent: id={}, orderId={}", event.getId(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to send outbox event: id={}", event.getId(), e);
            }
        }
    }
}