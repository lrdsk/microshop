package com.example.microshop.notification_service.controller;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.service.SearchOrderRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderRecordController {
    private final SearchOrderRecordService searchOrderRecordService;

    @GetMapping("/all")
    public List<OrderRecordResponseDTO> getAllOrderRecords() {
        log.info("REST request to find orders");
        return searchOrderRecordService.findAll();
    }

    @GetMapping("/by-order/{orderId}")
    public List<OrderRecordResponseDTO> getAllOrderRecordsByOrderId(@PathVariable("orderId") UUID orderId) {
        return searchOrderRecordService.findByOrderId(orderId);
    }

    @GetMapping("/by-user/{userId}")
    public List<OrderRecordResponseDTO> getAllOrderRecordsByUserId(@PathVariable("userId") UUID userId) {
        return searchOrderRecordService.findByUserId(userId);
    }
}
