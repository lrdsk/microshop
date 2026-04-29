package com.example.microshop.inventory_service.adapter.grpc;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.exception.InsufficientStockException;
import com.example.microshop.inventory_service.interceptor.TraceIdGrpcServerInterceptor;
import com.example.microshop.inventory_service.service.ProductService;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import io.grpc.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.MDC;

import java.util.*;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductService productService;

    @Override
    public void checkAvailabilityBatch(Inventory.ProductRequestList requestList,
                                       io.grpc.stub.StreamObserver<Inventory.ProductResponseList> responseObserver) {
        String traceId = TraceIdGrpcServerInterceptor.TRACE_ID_CONTEXT_KEY.get();
        MDC.put("X-Trace-Id", traceId);
        try {
            List<Inventory.ProductRequest> requests = requestList.getRequestsList();
            if (requests.isEmpty()) {
                responseObserver.onNext(Inventory.ProductResponseList.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }

            Map<UUID, Integer> requiredQuantities = new LinkedHashMap<>();
            List<UUID> productIds = new ArrayList<>();

            for (Inventory.ProductRequest req : requests) {
                UUID productId;
                try {
                    productId = UUID.fromString(req.getProductId());
                } catch (IllegalArgumentException e) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Invalid product_id format: " + req.getProductId())
                            .asRuntimeException());
                    return;
                }
                productIds.add(productId);
                requiredQuantities.put(productId, req.getQuantity());
            }

            Map<UUID, Product> productsMap = productService.findAllByIds(productIds);
            for (UUID id : productIds) {
                if (!productsMap.containsKey(id)) {
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Product not found: " + id)
                            .asRuntimeException());
                    return;
                }
            }

            for (Map.Entry<UUID, Integer> entry : requiredQuantities.entrySet()) {
                UUID currentProductId = entry.getKey();
                int currentQuantity = entry.getValue();
                Product product = productsMap.get(currentProductId);

                if (product.getQuantity() < currentQuantity) {
                    responseObserver.onError(Status.FAILED_PRECONDITION
                            .withDescription("Not enough stock for product " + currentProductId +
                                    ". Available: " + product.getQuantity() +
                                    ", requested: " + currentQuantity)
                            .asRuntimeException());
                    return;
                }
            }
            Map<UUID, Product> updatedProducts = new HashMap<>();
            try {
                updatedProducts = productService.batchReduceQuantities(requiredQuantities);
            } catch (InsufficientStockException e) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription(e.getMessage())
                        .asRuntimeException());
            }
            List<Inventory.ProductResponse> responses = new ArrayList<>(requests.size());

            for (Inventory.ProductRequest req : requests) {
                UUID productId = UUID.fromString(req.getProductId());
                Product product = updatedProducts.get(productId);
                Inventory.ProductResponse response = Inventory.ProductResponse.newBuilder()
                        .setProductId(product.getId().toString())
                        .setName(product.getName())
                        .setQuantity(product.getQuantity())
                        .setPrice(product.getPrice())
                        .setSale(product.getSale())
                        .build();
                responses.add(response);
            }

            Inventory.ProductResponseList responseList = Inventory.ProductResponseList.newBuilder()
                    .addAllResponses(responses)
                    .build();
            responseObserver.onNext(responseList);
            responseObserver.onCompleted();

        } finally {
            MDC.remove("X-Trace-Id");
        }
    }
}