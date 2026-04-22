package com.example.microshop.order_service.controller;

import com.example.microshop.order_service.dto.ProductRequest;
import com.example.microshop.order_service.service.OrderService;
import com.example.microshop.order_service.service.grpc.InventoryGrpcClient;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import inventory.Inventory;
import io.grpc.StatusRuntimeException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final InventoryGrpcClient inventoryClient;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody List<ProductRequest> productsRequest,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        Map<Inventory.ProductResponse, Integer> products = new HashMap<>();
        for (ProductRequest productRequest : productsRequest) {
            Inventory.ProductResponse currentProduct;
            try {
                currentProduct = inventoryClient.checkProduct(productRequest.productId().toString(), productRequest.quantity());
            } catch (StatusRuntimeException e) {
                return ResponseEntity.badRequest().body("Product check failed: " + e.getMessage());
            }

            if (currentProduct.getQuantity() < productRequest.quantity()) {
                return ResponseEntity.badRequest().body("Not enough stock");
            }
            products.put(currentProduct, productRequest.quantity());
        }

        CreateOrderCommand createOrderCommand = mapCreateOrderCommand(userDetails, products);
        orderService.createOrder(createOrderCommand);

        return ResponseEntity.ok("Order has been created");
    }

    private static @NotNull CreateOrderCommand mapCreateOrderCommand(UserDetails userDetails, Map<Inventory.ProductResponse, Integer> products) {
        return new CreateOrderCommand(
                userDetails.getUsername(),
                products.entrySet()
                        .stream()
                        .map(currentProduct -> new CreateOrderCommand.OrderItemValue(
                                UUID.fromString(currentProduct.getKey().getProductId()),
                                currentProduct.getValue(),
                                currentProduct.getKey().getPrice(),
                                currentProduct.getKey().getSale())).toList()
        );
    }
}
