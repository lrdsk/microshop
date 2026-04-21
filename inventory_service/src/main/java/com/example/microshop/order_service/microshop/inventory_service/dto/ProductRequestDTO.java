package com.example.microshop.order_service.microshop.inventory_service.dto;

import java.util.UUID;

public record ProductRequestDTO(UUID id,
                                String name,
                                Integer quantity,
                                Double price,
                                Integer sale) {
}
