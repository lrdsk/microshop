package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.dto.ProductRequest;
import com.example.microshop.order_service.exception.ProductCheckFailedException;
import com.example.microshop.order_service.service.CreatorOrderService;
import com.example.microshop.order_service.service.OrderService;
import com.example.microshop.order_service.service.grpc.InventoryGrpcClient;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import inventory.Inventory;
import io.grpc.StatusRuntimeException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreatorOrderServiceImpl implements CreatorOrderService {

    private final InventoryGrpcClient inventoryClient;
    private final OrderService orderService;

    @Override
    public void findProductsAndCreateOrder(List<ProductRequest> productsRequest, UUID userId) {
        List<Inventory.ProductRequest> grpcRequests = getGrpcRequests(productsRequest);

        List<Inventory.ProductResponse> productResponses;
        try {
            productResponses = inventoryClient.checkProductsBatch(grpcRequests);
        } catch (StatusRuntimeException e) {
            throw new ProductCheckFailedException("Batch product check failed: " + e.getMessage());
        }

        HashMap<Inventory.ProductResponse, Integer> productsMap = new HashMap<>();
        for (int i = 0; i < productsRequest.size(); i++) {
            ProductRequest originalRequest = productsRequest.get(i);
            Inventory.ProductResponse currentResponse = productResponses.get(i);
            productsMap.put(currentResponse, originalRequest.quantity());
        }

        CreateOrderCommand createOrderCommand = mapCreateOrderCommand(userId, productsMap);
        orderService.createOrder(createOrderCommand);
    }

    private static List<Inventory.ProductRequest> getGrpcRequests(List<ProductRequest> productsRequest) {
        return productsRequest.stream()
                .map(currentProductRequest -> Inventory.ProductRequest.newBuilder()
                        .setProductId(currentProductRequest.productId().toString())
                        .setQuantity(currentProductRequest.quantity())
                        .build())
                .collect(Collectors.toList());
    }

    private static @NotNull CreateOrderCommand mapCreateOrderCommand(
            UUID userId,
            java.util.Map<Inventory.ProductResponse, Integer> products) {

        return new CreateOrderCommand(
                userId,
                products.entrySet()
                        .stream()
                        .map(entry -> new CreateOrderCommand.OrderItemValue(
                                UUID.fromString(entry.getKey().getProductId()),
                                entry.getValue(),
                                entry.getKey().getPrice(),
                                entry.getKey().getSale()))
                        .collect(Collectors.toList())
        );
    }
}