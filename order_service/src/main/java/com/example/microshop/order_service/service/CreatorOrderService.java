package com.example.microshop.order_service.service;

import com.example.microshop.order_service.dto.ProductRequest;

import java.util.List;
import java.util.UUID;

public interface CreatorOrderService {
    void findProductsAndCreateOrder(List<ProductRequest> productsRequest, UUID userId);
}
