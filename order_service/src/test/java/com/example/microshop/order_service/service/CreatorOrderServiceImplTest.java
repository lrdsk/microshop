package com.example.microshop.order_service.service;

import com.example.microshop.order_service.dto.ProductRequest;
import com.example.microshop.order_service.service.grpc.InventoryGrpcClient;
import com.example.microshop.order_service.service.order.CreatorOrderServiceImpl;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import inventory.Inventory;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для CreatorOrderServiceImpl")
class CreatorOrderServiceImplTest {

    @Mock
    private InventoryGrpcClient inventoryClient;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CreatorOrderServiceImpl creatorOrderService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String PRODUCT_ID_STR = PRODUCT_ID.toString();
    private static final int QUANTITY = 2;
    private static final double PRICE = 100.0;
    private static final int SALE = 10;

    @Test
    @DisplayName("Успешное создание заказа: проверка gRPC и вызов orderService")
    void shouldCreateOrderSuccessfully() {
        //given
        ProductRequest request = new ProductRequest(PRODUCT_ID, QUANTITY);
        Inventory.ProductResponse productResponse = Inventory.ProductResponse.newBuilder()
                .setProductId(PRODUCT_ID_STR)
                .setQuantity(10)
                .setPrice(PRICE)
                .setSale(SALE)
                .build();
        when(inventoryClient.checkProduct(PRODUCT_ID_STR, QUANTITY)).thenReturn(productResponse);

        //when
        creatorOrderService.findProductsAndCreateOrder(List.of(request), USER_ID);

        //then
        verify(inventoryClient, times(1)).checkProduct(PRODUCT_ID_STR, QUANTITY);
        ArgumentCaptor<CreateOrderCommand> captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(orderService, times(1)).createOrder(captor.capture());
        CreateOrderCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(USER_ID);
        assertThat(command.orderItemValues()).hasSize(1);
        assertThat(command.orderItemValues().get(0).productId()).isEqualTo(PRODUCT_ID);
        assertThat(command.orderItemValues().get(0).quantity()).isEqualTo(QUANTITY);
        assertThat(command.orderItemValues().get(0).price()).isEqualTo(PRICE);
        assertThat(command.orderItemValues().get(0).sale()).isEqualTo(SALE);
    }

    @Test
    @DisplayName("Ошибка gRPC -> выбрасывается RuntimeException")
    void shouldThrowExceptionWhenGrpcFails() {
        //given
        ProductRequest request = new ProductRequest(PRODUCT_ID, QUANTITY);
        StatusRuntimeException grpcException = Status.UNAVAILABLE.withDescription("gRPC unavailable").asRuntimeException();
        when(inventoryClient.checkProduct(PRODUCT_ID_STR, QUANTITY)).thenThrow(grpcException);

        //when then
        assertThatThrownBy(() -> creatorOrderService.findProductsAndCreateOrder(List.of(request), USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product check failed");
        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Недостаточно товара на складе -> IllegalStateException")
    void shouldThrowExceptionWhenNotEnoughStock() {
        //given
        ProductRequest request = new ProductRequest(PRODUCT_ID, QUANTITY);
        Inventory.ProductResponse productResponse = Inventory.ProductResponse.newBuilder()
                .setProductId(PRODUCT_ID_STR)
                .setQuantity(1)
                .setPrice(PRICE)
                .setSale(SALE)
                .build();
        when(inventoryClient.checkProduct(PRODUCT_ID_STR, QUANTITY)).thenReturn(productResponse);

        //when then
        assertThatThrownBy(() -> creatorOrderService.findProductsAndCreateOrder(List.of(request), USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough stock");
        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Обработка нескольких товаров в заказе")
    void shouldHandleMultipleProducts() {
        //given
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        String productId1Str = productId1.toString();
        String productId2Str = productId2.toString();
        ProductRequest request1 = new ProductRequest(productId1, 1);
        ProductRequest request2 = new ProductRequest(productId2, 3);
        Inventory.ProductResponse productResponse1 = Inventory.ProductResponse.newBuilder()
                .setProductId(productId1Str)
                .setQuantity(5)
                .setPrice(50.0)
                .setSale(0)
                .build();
        Inventory.ProductResponse productResponse2 = Inventory.ProductResponse.newBuilder()
                .setProductId(productId2Str)
                .setQuantity(10)
                .setPrice(30.0)
                .setSale(5)
                .build();
        when(inventoryClient.checkProduct(productId1Str, 1)).thenReturn(productResponse1);
        when(inventoryClient.checkProduct(productId2Str, 3)).thenReturn(productResponse2);

        //when
        creatorOrderService.findProductsAndCreateOrder(List.of(request1, request2), USER_ID);

        //then
        ArgumentCaptor<CreateOrderCommand> captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(orderService).createOrder(captor.capture());
        CreateOrderCommand command = captor.getValue();
        assertThat(command.orderItemValues()).hasSize(2);
        assertThat(command.orderItemValues()).anyMatch(item -> item.productId().equals(productId1) && item.quantity() == 1);
        assertThat(command.orderItemValues()).anyMatch(item -> item.productId().equals(productId2) && item.quantity() == 3);
    }
}
