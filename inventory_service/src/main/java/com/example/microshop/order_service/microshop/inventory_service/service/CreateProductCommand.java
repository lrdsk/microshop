package com.example.microshop.order_service.microshop.inventory_service.service;

import java.util.UUID;

public record CreateProductCommand(UUID id,
                                   String name,
                                   Integer quantity,
                                   Double price,
                                   Integer sale) {
}
