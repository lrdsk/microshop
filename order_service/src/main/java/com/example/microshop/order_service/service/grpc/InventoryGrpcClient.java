package com.example.microshop.order_service.service.grpc;

import com.example.microshop.order_service.interceptor.ApiKeyClientInterceptor;
import com.example.microshop.order_service.interceptor.TraceIdGrpcClientInterceptor;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryGrpcClient {

    @GrpcClient(value = "inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    @Value("${internal.api.key}")
    private String apiKey;

    /**
     * Batch-проверка нескольких продуктов за один вызов.
     * @param requests список пар (productId, quantity)
     * @return список ответов для каждого продукта в том же порядке
     */
    public List<Inventory.ProductResponse> checkProductsBatch(List<Inventory.ProductRequest> requests) {
        Inventory.ProductRequestList requestList = Inventory.ProductRequestList.newBuilder()
                .addAllRequests(requests)
                .build();
        Inventory.ProductResponseList responseList = getStubWithTraceAndInternalApiKey()
                .checkAvailabilityBatch(requestList);
        return responseList.getResponsesList();
    }

    private InventoryServiceGrpc.InventoryServiceBlockingStub getStubWithTraceAndInternalApiKey() {
        return inventoryStub.withInterceptors(
                new TraceIdGrpcClientInterceptor(),
                new ApiKeyClientInterceptor(apiKey));
    }
}