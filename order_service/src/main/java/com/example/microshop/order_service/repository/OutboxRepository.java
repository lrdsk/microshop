package com.example.microshop.order_service.repository;

import com.example.microshop.order_service.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, Integer> {
    @Query("SELECT o FROM OutboxEntity o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<OutboxEntity> findOutboxEntityByStatusPending(Pageable pageable);
}
