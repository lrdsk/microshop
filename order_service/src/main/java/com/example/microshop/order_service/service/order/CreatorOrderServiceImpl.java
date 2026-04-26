package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.dto.ProductRequest;
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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorOrderServiceImpl implements CreatorOrderService {
    private final InventoryGrpcClient inventoryClient;
    private final OrderService orderService;

    @Override
    public void findProductsAndCreateOrder(List<ProductRequest> productsRequest, UUID userId) {
        Map<Inventory.ProductResponse, Integer> products = new HashMap<>();
        for (ProductRequest productRequest : productsRequest) {
            Inventory.ProductResponse currentProduct;
            try {
                currentProduct = inventoryClient.checkProduct(productRequest.productId().toString(), productRequest.quantity());
            } catch (StatusRuntimeException e) {
                throw new RuntimeException("Product check failed: " + e.getMessage());
            }

            if (currentProduct.getQuantity() < productRequest.quantity()) {
                throw new IllegalStateException("Not enough stock");
            }
            products.put(currentProduct, productRequest.quantity());
        }

        CreateOrderCommand createOrderCommand = mapCreateOrderCommand(userId, products);
        orderService.createOrder(createOrderCommand);

    }

    private static @NotNull CreateOrderCommand mapCreateOrderCommand(UUID userId, Map<Inventory.ProductResponse, Integer> products) {
        return new CreateOrderCommand(
                userId,
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
