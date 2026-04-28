package com.example.microshop.inventory_service.service.grpc;

import com.example.microshop.inventory_service.adapter.grpc.InventoryGrpcServiceImpl;
import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.service.ProductService;
import inventory.Inventory;
import inventory.InventoryServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для InventoryGrpcServiceImpl")
class InventoryGrpcServiceImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private StreamObserver<Inventory.ProductResponse> responseObserver;

    @InjectMocks
    private InventoryGrpcServiceImpl grpcService;

    private static final UUID PRODUCT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String PRODUCT_ID_STR = PRODUCT_ID.toString();
    private static final String PRODUCT_NAME = "Laptop";
    private static final int QUANTITY = 5;
    private static final double PRICE = 999.99;
    private static final int SALE = 10;
    private static final int REDUCED_QUANTITY = 45;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    @DisplayName("checkAvailability: успешный сценарий")
    void checkAvailability_Success() {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR)
                .setQuantity(QUANTITY)
                .build();

        Product product = ProductFactory.createProduct(PRODUCT_ID, PRODUCT_NAME, 50, PRICE, SALE);
        when(productService.findById(PRODUCT_ID)).thenReturn(product);
        when(productService.reduceProductQuantity(PRODUCT_ID, QUANTITY)).thenReturn(REDUCED_QUANTITY);

        grpcService.checkAvailability(request, responseObserver);

        // Проверяем, что сервис был вызван
        verify(productService).findById(PRODUCT_ID);
        verify(productService).reduceProductQuantity(PRODUCT_ID, QUANTITY);

        // Перехватываем ответ
        ArgumentCaptor<Inventory.ProductResponse> responseCaptor = ArgumentCaptor.forClass(Inventory.ProductResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        Inventory.ProductResponse response = responseCaptor.getValue();
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID_STR);
        assertThat(response.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(response.getQuantity()).isEqualTo(REDUCED_QUANTITY);
        assertThat(response.getPrice()).isEqualTo(PRICE);
        assertThat(response.getSale()).isEqualTo(SALE);
    }

    @Test
    @DisplayName("checkAvailability: неверный формат product_id -> INVALID_ARGUMENT")
    void checkAvailability_InvalidProductIdFormat() {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId("invalid-uuid")
                .setQuantity(QUANTITY)
                .build();

        grpcService.checkAvailability(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> exceptionCaptor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(exceptionCaptor.capture());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = exceptionCaptor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(exception.getStatus().getDescription()).contains("Invalid product_id format");
    }

    @Test
    @DisplayName("checkAvailability: продукт не найден -> NOT_FOUND")
    void checkAvailability_ProductNotFound() {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR)
                .setQuantity(QUANTITY)
                .build();

        when(productService.findById(PRODUCT_ID)).thenThrow(new EntityNotFoundException("Product not found"));

        grpcService.checkAvailability(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> exceptionCaptor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(exceptionCaptor.capture());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = exceptionCaptor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
        assertThat(exception.getStatus().getDescription()).contains("Product with id");
    }

    @Test
    @DisplayName("checkAvailability: ошибка при reduceProductQuantity (например, недостаточно товара) -> ошибка передаётся в ответ")
    void checkAvailability_ReduceQuantityFails() {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR)
                .setQuantity(QUANTITY)
                .build();

        Product product = ProductFactory.createProduct(PRODUCT_ID, PRODUCT_NAME, 3, PRICE, SALE); // остаток 3, а запрашивают 5
        when(productService.findById(PRODUCT_ID)).thenReturn(product);
        when(productService.reduceProductQuantity(PRODUCT_ID, QUANTITY))
                .thenThrow(new IllegalStateException("Not enough stock"));

        grpcService.checkAvailability(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> exceptionCaptor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(exceptionCaptor.capture());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = exceptionCaptor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
    }
}
