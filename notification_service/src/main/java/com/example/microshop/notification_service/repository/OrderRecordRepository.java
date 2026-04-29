package com.example.microshop.notification_service.repository;

import com.example.microshop.notification_service.entity.OrderRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecordEntity, UUID> {
    List<OrderRecordEntity> findByOrderId(UUID orderId);
    List<OrderRecordEntity> findByUserId(UUID userId);
}
