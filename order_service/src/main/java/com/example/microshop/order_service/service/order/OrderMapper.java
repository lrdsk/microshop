package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.domain.Order;
import com.example.microshop.order_service.entity.OrderEntity;
import com.example.microshop.order_service.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderEntity toEntity(Order order) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getOrderId());
        orderEntity.setUserId(order.getUserId());
        orderEntity.setTotalAmount(new BigDecimal(order.getTotalAmount()));

        List<OrderItemEntity> orderItemEntityList = order.getItems().stream()
                .map(item -> {
                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setId(item.getItemId());
                    itemEntity.setProductId(item.getProductId());
                    itemEntity.setQuantity(item.getQuantity());
                    itemEntity.setPrice(new BigDecimal(item.getPrice()));
                    itemEntity.setSale(item.getSale());
                    itemEntity.setTotalPrice(new BigDecimal(item.getPrice() * item.getQuantity() * (100 - item.getSale()) / 100.0));
                    itemEntity.setOrder(orderEntity);
                    return itemEntity;
                }).collect(Collectors.toList());

        orderEntity.setItems(orderItemEntityList);

        return orderEntity;
    }
}
