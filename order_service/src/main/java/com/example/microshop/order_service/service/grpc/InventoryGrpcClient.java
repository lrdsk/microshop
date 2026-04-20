package com.example.microshop.order_service.service.grpc;

import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryGrpcClient {
    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    public Inventory.ProductResponse checkProduct(String productId) {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(productId)
                .build();
        return inventoryStub.checkAvailability(request);
    }
}