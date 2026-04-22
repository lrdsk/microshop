package com.example.microshop.inventory_service.service;

public record CreateProductCommand(
        String name,
        Integer quantity,
        Double price,
        Integer sale) {
}
