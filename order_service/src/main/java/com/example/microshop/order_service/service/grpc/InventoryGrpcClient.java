package com.example.microshop.order_service.service.grpc;

import com.example.microshop.order_service.interceptor.ApiKeyClientInterceptor;
import com.example.microshop.order_service.interceptor.TraceIdGrpcClientInterceptor;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryGrpcClient {
    @GrpcClient(value = "inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    @Value("${internal.api.key}")
    private String apiKey;

    public Inventory.ProductResponse checkProduct(String productId, int quantity) {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();
        return getStubWithTraceAndInternalApiKey().checkAvailability(request);
    }

    private InventoryServiceGrpc.InventoryServiceBlockingStub getStubWithTraceAndInternalApiKey() {
        return inventoryStub.withInterceptors(
                new TraceIdGrpcClientInterceptor(),
                new ApiKeyClientInterceptor(apiKey));
    }
}