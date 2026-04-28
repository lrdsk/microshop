package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.domain.Order;
import com.example.microshop.order_service.domain.OrderFactory;
import com.example.microshop.order_service.domain.OrderItem;
import com.example.microshop.order_service.entity.AggregateType;
import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OrderEntity;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.order_service.repository.OrderRepository;
import com.example.microshop.order_service.repository.OutboxRepository;
import com.example.microshop.order_service.service.OrderService;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void createOrder(CreateOrderCommand createOrderCommand) {
        log.info("Called orderService to create new order: {}", createOrderCommand);
        Order order = OrderFactory.createOrder(createOrderCommand.userId());

        List<OrderItem> orderItems = createOrderCommand.orderItemValues().stream()
                .map(orderItemValue ->
                        OrderFactory.createOrderItem(
                                orderItemValue.productId(),
                                orderItemValue.quantity(),
                                orderItemValue.price(),
                                orderItemValue.sale()
                        ))
                .toList();

        orderItems.forEach(order::addItem);

        OrderEntity orderEntity = orderMapper.toEntity(order);
        orderRepository.save(orderEntity);

        OutboxEntity outboxEntity = buildOutboxEvent(orderEntity);
        outboxRepository.save(outboxEntity);

    }

    private OutboxEntity buildOutboxEvent(OrderEntity order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemEvent(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice().doubleValue(),
                                item.getSale(),
                                item.getTotalPrice().doubleValue()
                        )).toList(),
                order.getTotalAmount().doubleValue(),
                LocalDateTime.now()
        );

        try {
            String currentTraceId = MDC.get("X-Trace-Id");
            String payload = objectMapper.writeValueAsString(event);

            OutboxEntity outbox = new OutboxEntity();
            outbox.setAggregateType(AggregateType.ORDER);
            outbox.setAggregateId(order.getId());
            outbox.setEventType("OrderCreated");
            outbox.setPayload(payload);
            outbox.setStatus(EventStatus.PENDING);
            outbox.setTraceId(UUID.fromString(currentTraceId));

            return outbox;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
