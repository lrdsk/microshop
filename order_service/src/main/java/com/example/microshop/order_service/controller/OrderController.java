package com.example.microshop.order_service.controller;

import com.example.microshop.order_service.dto.OrderRequest;
import com.example.microshop.order_service.service.grpc.InventoryGrpcClient;
import inventory.Inventory;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final InventoryGrpcClient inventoryClient;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        Inventory.ProductResponse product;
        try {
            product = inventoryClient.checkProduct(request.productId().toString());
        } catch (StatusRuntimeException e) {
            return ResponseEntity.badRequest().body("Product check failed: " + e.getMessage());
        }

        if (product.getQuantity() < request.quantity()) {
            return ResponseEntity.badRequest().body("Not enough stock");
        }

        return ResponseEntity.ok("Order has been created");
    }
}
