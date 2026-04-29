package com.example.microshop.order_service.service;

import com.example.microshop.order_service.service.order.command.CreateOrderCommand;

public interface OrderService {
    void createOrder(CreateOrderCommand createOrderCommand);
}
