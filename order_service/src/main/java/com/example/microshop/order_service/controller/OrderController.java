package com.example.microshop.order_service.controller;

import com.example.microshop.order_service.dto.ProductRequest;
import com.example.microshop.order_service.service.CreatorOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final CreatorOrderService createOrderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody List<ProductRequest> productsRequest,
                                              @RequestHeader("X-User-Id") String userId) {

        createOrderService.findProductsAndCreateOrder(productsRequest, UUID.fromString(userId));
        return ResponseEntity.ok("Order has been created");
    }
}
