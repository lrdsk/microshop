package com.example.microshop.order_service.exception;

public class ProductCheckFailedException extends RuntimeException {
    public ProductCheckFailedException(String message) {
        super(message);
    }
}
