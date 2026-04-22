package com.example.microshop.inventory_service.adapter.grpc;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.entity.ProductEntity;
import com.example.microshop.inventory_service.repository.ProductRepository;
import com.example.microshop.inventory_service.service.ProductService;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {
    private final ProductService productService;
    @Override
    public void checkAvailability(Inventory.ProductRequest request,
                                  io.grpc.stub.StreamObserver<Inventory.ProductResponse> responseObserver) {
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

        int reducedValue = productService.reduceProductQuantity(product.getId(), request.getQuantity());

        Inventory.ProductResponse response = Inventory.ProductResponse.newBuilder()
                .setProductId(product.getId().toString())
                .setName(product.getName())
                .setQuantity(reducedValue)
                .setPrice(product.getPrice())
                .setSale(product.getSale())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
