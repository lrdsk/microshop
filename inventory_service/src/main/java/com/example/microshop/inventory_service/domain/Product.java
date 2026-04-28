package com.example.microshop.inventory_service.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Product {
    private final UUID id;

    private String name;

    private int quantity;

    private double price;

    private int sale;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    Product(UUID id, String name, int quantity, double price, int sale) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.sale = sale;
    }

    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }


    public int reduceQuantity(int quantity) {
        if(this.quantity < quantity) {
           throw new IllegalStateException("Not enough stock");
        }

        this.quantity = this.quantity - quantity;
        return quantity;
    }
}
