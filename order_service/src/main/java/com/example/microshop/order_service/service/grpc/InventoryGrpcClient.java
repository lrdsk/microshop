package com.example.microshop.order_service.service.grpc;

import com.example.microshop.order_service.interceptor.TraceIdGrpcClientInterceptor;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryGrpcClient {
    @GrpcClient(value = "inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    public Inventory.ProductResponse checkProduct(String productId, int quantity) {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();
        return getStubWithTrace().checkAvailability(request);
    }

    private InventoryServiceGrpc.InventoryServiceBlockingStub getStubWithTrace() {
        return inventoryStub.withInterceptors(new TraceIdGrpcClientInterceptor());
    }
}