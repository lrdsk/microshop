package com.example.microshop.inventory_service.adapter.grpc;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.interceptor.TraceIdGrpcServerInterceptor;
import com.example.microshop.inventory_service.service.ProductService;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import io.grpc.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.MDC;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {
    private final ProductService productService;

    @Override
    public void checkAvailability(Inventory.ProductRequest request,
                                  io.grpc.stub.StreamObserver<Inventory.ProductResponse> responseObserver) {
        String traceId = TraceIdGrpcServerInterceptor.TRACE_ID_CONTEXT_KEY.get();
        MDC.put("X-Trace-Id", traceId);
        try {
            String productIdStr = request.getProductId();

            UUID productId;
            try {
                productId = UUID.fromString(productIdStr);
            } catch (IllegalArgumentException e) {
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                        .withDescription("Invalid product_id format, should be UUID format")
                        .asRuntimeException());
                return;
            }

            Product product;
            try {
                product = productService.findById(productId);
            } catch (EntityNotFoundException e) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Product with id '%s' not found".formatted(productId))
                        .asRuntimeException());
                return;
            }
            int reducedValue;
            try {
                reducedValue = productService.reduceProductQuantity(product.getId(), request.getQuantity());
            } catch (IllegalStateException e) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Get current quantity '%s' of product impossible, not enough stock".formatted(productId))
                        .asRuntimeException());
                return;
            }

            Inventory.ProductResponse response = Inventory.ProductResponse.newBuilder()
                    .setProductId(product.getId().toString())
                    .setName(product.getName())
                    .setQuantity(reducedValue)
                    .setPrice(product.getPrice())
                    .setSale(product.getSale())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            MDC.remove("X-Trace-Id");
        }
    }
}
