package com.example.microshop.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@NoArgsConstructor
@Getter
@Setter
public class OutboxEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;
    private UUID aggregateId;
    private String eventType;
    private String payload;
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
