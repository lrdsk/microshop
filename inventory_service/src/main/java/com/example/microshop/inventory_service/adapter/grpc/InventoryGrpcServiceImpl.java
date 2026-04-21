package com.example.microshop.inventory_service.adapter.grpc;

import com.example.microshop.inventory_service.entity.ProductEntity;
import com.example.microshop.inventory_service.repository.ProductRepository;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {
    private final ProductRepository productRepository;
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

        ProductEntity product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Product with id '%s' not found".formatted(productId))
                    .asRuntimeException());
            return;
        }

        Inventory.ProductResponse response = Inventory.ProductResponse.newBuilder()
                .setProductId(product.getId().toString())
                .setName(product.getName())
                .setQuantity(product.getQuantity())
                .setPrice(product.getPrice())
                .setSale(product.getSale())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
