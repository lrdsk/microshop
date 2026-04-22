package com.example.microshop.inventory_service.service;

import com.example.microshop.inventory_service.domain.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> findAll();
    Product findById(UUID id);
    void createProduct(CreateProductCommand command);
    void deleteProduct(UUID id);
    int reduceProductQuantity(UUID id, int quantity);
}
