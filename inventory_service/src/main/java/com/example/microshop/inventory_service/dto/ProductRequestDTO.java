package com.example.microshop.inventory_service.dto;

public record ProductRequestDTO(
        String name,
        Integer quantity,
        Double price,
        Integer sale) {
}
