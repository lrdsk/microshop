package com.example.microshop.order_service.controller;

import com.example.microshop.order_service.dto.ProductRequest;
import com.example.microshop.order_service.service.CreatorOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("Интеграционные тесты для OrderController")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatorOrderService createOrderService;

    @Test
    @DisplayName("POST /api/order -> успешное создание заказа")
    void createOrderShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        ProductRequest productRequest = new ProductRequest(UUID.randomUUID(), 2);
        List<ProductRequest> requestBody = List.of(productRequest);

        doNothing().when(createOrderService)
                .findProductsAndCreateOrder(any(), eq(userId));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Order has been created"));
    }
}