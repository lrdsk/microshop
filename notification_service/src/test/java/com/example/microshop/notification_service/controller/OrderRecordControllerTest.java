package com.example.microshop.notification_service.controller;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.service.SearchOrderRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderRecordController.class)
@DisplayName("Интеграционные тесты для OrderRecordController")
class OrderRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchOrderRecordService searchOrderRecordService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/orders/all возвращает список всех записей")
    void getAllOrderRecordsShouldReturnList() throws Exception {
        OrderRecordResponseDTO dto = new OrderRecordResponseDTO(
                UUID.randomUUID(), ORDER_ID, PRODUCT_ID, 2, 100.0, 10, 180.0, USER_ID);
        when(searchOrderRecordService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].orderId").value(ORDER_ID.toString()));
    }

    @Test
    @DisplayName("GET /api/orders/by-order/{orderId} возвращает записи по orderId")
    void getOrdersByOrderIdShouldReturnMatchingRecords() throws Exception {
        OrderRecordResponseDTO dto = new OrderRecordResponseDTO(
                UUID.randomUUID(), ORDER_ID, PRODUCT_ID, 2, 100.0, 10, 180.0, USER_ID);
        when(searchOrderRecordService.findByOrderId(ORDER_ID)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orders/by-order/{id}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(ORDER_ID.toString()));
    }

    @Test
    @DisplayName("GET /api/orders/by-user/{userId} возвращает записи по userId")
    void getOrdersByUserIdShouldReturnMatchingRecords() throws Exception {
        OrderRecordResponseDTO dto = new OrderRecordResponseDTO(
                UUID.randomUUID(), ORDER_ID, PRODUCT_ID, 2, 100.0, 10, 180.0, USER_ID);
        when(searchOrderRecordService.findByUserId(USER_ID)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orders/by-user/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(USER_ID.toString()));
    }
}
