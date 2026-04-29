package com.example.microshop.inventory_service.service.grpc;

import com.example.microshop.inventory_service.adapter.grpc.InventoryGrpcServiceImpl;
import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.service.ProductService;
import inventory.Inventory;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для InventoryGrpcServiceImpl (batch)")
class InventoryGrpcServiceImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private StreamObserver<Inventory.ProductResponseList> responseObserver;

    @InjectMocks
    private InventoryGrpcServiceImpl grpcService;

    private static final UUID PRODUCT_ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String PRODUCT_ID_STR_1 = PRODUCT_ID_1.toString();
    private static final String PRODUCT_NAME_1 = "Laptop";
    private static final int QUANTITY_1 = 2;
    private static final double PRICE_1 = 999.99;
    private static final int SALE_1 = 10;

    private static final UUID PRODUCT_ID_2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
    private static final String PRODUCT_ID_STR_2 = PRODUCT_ID_2.toString();
    private static final String PRODUCT_NAME_2 = "Mouse";
    private static final int QUANTITY_2 = 5;
    private static final double PRICE_2 = 29.99;
    private static final int SALE_2 = 0;

    private static final int STOCK_1 = 100;
    private static final int STOCK_2 = 50;
    private static final int NEW_STOCK_1 = STOCK_1 - QUANTITY_1;
    private static final int NEW_STOCK_2 = STOCK_2 - QUANTITY_2;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    @DisplayName("checkAvailabilityBatch: успешный сценарий с одним продуктом")
    void shouldSuccessfullyCheckOneProduct() {
        // given
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR_1)
                .setQuantity(QUANTITY_1)
                .build();
        Inventory.ProductRequestList requestList = Inventory.ProductRequestList.newBuilder()
                .addRequests(request)
                .build();

        Product product = ProductFactory.createProduct(PRODUCT_ID_1, PRODUCT_NAME_1, STOCK_1, PRICE_1, SALE_1);
        Product updatedProduct = ProductFactory.createProduct(PRODUCT_ID_1, PRODUCT_NAME_1, NEW_STOCK_1, PRICE_1, SALE_1);

        when(productService.findAllByIds(List.of(PRODUCT_ID_1)))
                .thenReturn(Map.of(PRODUCT_ID_1, product));
        when(productService.batchReduceQuantities(Map.of(PRODUCT_ID_1, QUANTITY_1)))
                .thenReturn(Map.of(PRODUCT_ID_1, updatedProduct));

        // when
        grpcService.checkAvailabilityBatch(requestList, responseObserver);

        // then
        verify(productService).findAllByIds(List.of(PRODUCT_ID_1));
        verify(productService).batchReduceQuantities(Map.of(PRODUCT_ID_1, QUANTITY_1));

        ArgumentCaptor<Inventory.ProductResponseList> captor = ArgumentCaptor.forClass(Inventory.ProductResponseList.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        Inventory.ProductResponseList responseList = captor.getValue();
        assertThat(responseList.getResponsesList()).hasSize(1);
        Inventory.ProductResponse response = responseList.getResponses(0);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID_STR_1);
        assertThat(response.getName()).isEqualTo(PRODUCT_NAME_1);
        assertThat(response.getQuantity()).isEqualTo(NEW_STOCK_1);
        assertThat(response.getPrice()).isEqualTo(PRICE_1);
        assertThat(response.getSale()).isEqualTo(SALE_1);
    }

    @Test
    @DisplayName("checkAvailabilityBatch: успешный сценарий с двумя продуктами")
    void shouldSuccessfullyCheckTwoProducts() {
        // given
        Inventory.ProductRequest request1 = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR_1)
                .setQuantity(QUANTITY_1)
                .build();
        Inventory.ProductRequest request2 = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR_2)
                .setQuantity(QUANTITY_2)
                .build();
        Inventory.ProductRequestList requestList = Inventory.ProductRequestList.newBuilder()
                .addAllRequests(List.of(request1, request2))
                .build();

        Product product1 = ProductFactory.createProduct(PRODUCT_ID_1, PRODUCT_NAME_1, STOCK_1, PRICE_1, SALE_1);
        Product product2 = ProductFactory.createProduct(PRODUCT_ID_2, PRODUCT_NAME_2, STOCK_2, PRICE_2, SALE_2);

        Product updatedProduct1 = ProductFactory.createProduct(PRODUCT_ID_1, PRODUCT_NAME_1, NEW_STOCK_1, PRICE_1, SALE_1);
        Product updatedProduct2 = ProductFactory.createProduct(PRODUCT_ID_2, PRODUCT_NAME_2, NEW_STOCK_2, PRICE_2, SALE_2);

        when(productService.findAllByIds(List.of(PRODUCT_ID_1, PRODUCT_ID_2)))
                .thenReturn(Map.of(PRODUCT_ID_1, product1, PRODUCT_ID_2, product2));
        when(productService.batchReduceQuantities(Map.of(PRODUCT_ID_1, QUANTITY_1, PRODUCT_ID_2, QUANTITY_2)))
                .thenReturn(Map.of(PRODUCT_ID_1, updatedProduct1, PRODUCT_ID_2, updatedProduct2));

        // when
        grpcService.checkAvailabilityBatch(requestList, responseObserver);

        // then
        verify(productService).findAllByIds(anyList());
        verify(productService).batchReduceQuantities(anyMap());

        ArgumentCaptor<Inventory.ProductResponseList> captor = ArgumentCaptor.forClass(Inventory.ProductResponseList.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        List<Inventory.ProductResponse> responses = captor.getValue().getResponsesList();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getProductId()).isEqualTo(PRODUCT_ID_STR_1);
        assertThat(responses.get(0).getQuantity()).isEqualTo(NEW_STOCK_1);
        assertThat(responses.get(1).getProductId()).isEqualTo(PRODUCT_ID_STR_2);
        assertThat(responses.get(1).getQuantity()).isEqualTo(NEW_STOCK_2);
    }

    @Test
    @DisplayName("checkAvailabilityBatch: пустой список запросов -> возвращает пустой ответ")
    void shouldReturnEmptyResponseWhenEmptyRequestList() {
        Inventory.ProductRequestList emptyList = Inventory.ProductRequestList.newBuilder().build();
        grpcService.checkAvailabilityBatch(emptyList, responseObserver);

        ArgumentCaptor<Inventory.ProductResponseList> captor = ArgumentCaptor.forClass(Inventory.ProductResponseList.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        assertThat(captor.getValue().getResponsesList()).isEmpty();
        verify(productService, never()).findAllByIds(anyList());
        verify(productService, never()).batchReduceQuantities(anyMap());
    }

    @Test
    @DisplayName("checkAvailabilityBatch: неверный формат product_id -> INVALID_ARGUMENT")
    void shouldReturnInvalidArgumentForWrongUuidFormat() {
        Inventory.ProductRequest invalidRequest = Inventory.ProductRequest.newBuilder()
                .setProductId("not-a-uuid")
                .setQuantity(QUANTITY_1)
                .build();
        Inventory.ProductRequestList requestList = Inventory.ProductRequestList.newBuilder()
                .addRequests(invalidRequest)
                .build();

        grpcService.checkAvailabilityBatch(requestList, responseObserver);

        ArgumentCaptor<StatusRuntimeException> captor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(captor.capture());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = captor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(exception.getStatus().getDescription()).contains("Invalid product_id format");
    }

    @Test
    @DisplayName("checkAvailabilityBatch: продукт не найден -> NOT_FOUND")
    void shouldReturnNotFoundWhenProductMissing() {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR_1)
                .setQuantity(QUANTITY_1)
                .build();
        Inventory.ProductRequestList requestList = Inventory.ProductRequestList.newBuilder()
                .addRequests(request)
                .build();

        when(productService.findAllByIds(List.of(PRODUCT_ID_1)))
                .thenReturn(Map.of());

        grpcService.checkAvailabilityBatch(requestList, responseObserver);

        ArgumentCaptor<StatusRuntimeException> captor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(captor.capture());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = captor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
        assertThat(exception.getStatus().getDescription()).contains("Product not found");
    }

    @Test
    @DisplayName("checkAvailabilityBatch: недостаточно товара на складе -> FAILED_PRECONDITION")
    void shouldReturnFailedPreconditionWhenNotEnoughStock() {
        Inventory.ProductRequest request = Inventory.ProductRequest.newBuilder()
                .setProductId(PRODUCT_ID_STR_1)
                .setQuantity(200)
                .build();
        Inventory.ProductRequestList requestList = Inventory.ProductRequestList.newBuilder()
                .addRequests(request)
                .build();

        Product product = ProductFactory.createProduct(PRODUCT_ID_1, PRODUCT_NAME_1, STOCK_1, PRICE_1, SALE_1);

        when(productService.findAllByIds(List.of(PRODUCT_ID_1)))
                .thenReturn(Map.of(PRODUCT_ID_1, product));

        grpcService.checkAvailabilityBatch(requestList, responseObserver);

        ArgumentCaptor<StatusRuntimeException> captor = ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(captor.capture());
        verify(responseObserver, never()).onCompleted();
        verify(productService, never()).batchReduceQuantities(anyMap());

        StatusRuntimeException exception = captor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.FAILED_PRECONDITION.getCode());
        assertThat(exception.getStatus().getDescription()).contains("Not enough stock");
    }
}