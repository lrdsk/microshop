package com.example.microshop.inventory_service.controller;

import com.example.microshop.inventory_service.adapter.controller.ProductController;
import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.dto.ProductRequestDTO;
import com.example.microshop.inventory_service.service.CreateProductCommand;
import com.example.microshop.inventory_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("Интеграционные тесты для ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private static final UUID PRODUCT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String PRODUCT_NAME = "Laptop";
    private static final Integer QUANTITY = 50;
    private static final Double PRICE = 1200.0;
    private static final Integer SALE = 10;
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime MODIFIED_AT = CREATED_AT.plusHours(1);

    @Test
    @DisplayName("GET /api/products -> возвращает список продуктов")
    void getAllProducts() throws Exception {
        Product product = ProductFactory.createProduct(PRODUCT_ID, PRODUCT_NAME, QUANTITY, PRICE, SALE);
        ProductFactory.setAudit(product, CREATED_AT, MODIFIED_AT);
        List<Product> products = List.of(product);

        when(productService.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$[0].name").value(PRODUCT_NAME))
                .andExpect(jsonPath("$[0].quantity").value(QUANTITY))
                .andExpect(jsonPath("$[0].price").value(PRICE))
                .andExpect(jsonPath("$[0].sale").value(SALE));

        verify(productService).findAll();
    }

    @Test
    @DisplayName("GET /api/products/{id} -> успешный поиск по id")
    void getProductById_Success() throws Exception {
        Product product = ProductFactory.createProduct(PRODUCT_ID, PRODUCT_NAME, QUANTITY, PRICE, SALE);
        ProductFactory.setAudit(product, CREATED_AT, MODIFIED_AT);

        when(productService.findById(PRODUCT_ID)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME))
                .andExpect(jsonPath("$.quantity").value(QUANTITY));

        verify(productService).findById(PRODUCT_ID);
    }

    /*@Test
    @DisplayName("GET /api/products/{id} -> продукт не найден, возвращает 404")
    void getProductById_NotFound() throws Exception {
        when(productService.findById(PRODUCT_ID)).thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", PRODUCT_ID))
                .andExpect(status().isNotFound());
    }*/

    @Test
    @DisplayName("POST /api/products -> создание продукта возвращает 201")
    void createProduct() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO(PRODUCT_NAME, QUANTITY, PRICE, SALE);
        doNothing().when(productService).createProduct(any(CreateProductCommand.class));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(productService).createProduct(any(CreateProductCommand.class));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} -> успешное удаление возвращает 200")
    void deleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(PRODUCT_ID);

        mockMvc.perform(delete("/api/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk());

        verify(productService).deleteProduct(PRODUCT_ID);
    }

    /*@Test
    @DisplayName("DELETE /api/products/{id} -> если продукт не найден, сервис бросает исключение -> 404")
    void deleteProduct_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Product not found")).when(productService).deleteProduct(PRODUCT_ID);

        mockMvc.perform(delete("/api/products/{id}", PRODUCT_ID))
                .andExpect(status().isNotFound());
    }*/
}
