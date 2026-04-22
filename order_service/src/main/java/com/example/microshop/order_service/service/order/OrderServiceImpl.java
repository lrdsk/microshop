package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.domain.Order;
import com.example.microshop.order_service.domain.OrderFactory;
import com.example.microshop.order_service.domain.OrderItem;
import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.entity.AggregateType;
import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OrderEntity;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.order_service.repository.OrderRepository;
import com.example.microshop.order_service.repository.OutboxRepository;
import com.example.microshop.order_service.service.OrderService;
import com.example.microshop.order_service.service.UserService;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void createOrder(CreateOrderCommand createOrderCommand) {
        log.info("Called orderService to create new order: {}", createOrderCommand);

        User user = userService.findUserByUsername(createOrderCommand.username());

        Order order = OrderFactory.createOrder(user.getId());

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
            String payload = objectMapper.writeValueAsString(event);

            OutboxEntity outbox = new OutboxEntity();
            outbox.setAggregateType(AggregateType.ORDER);
            outbox.setAggregateId(order.getId());
            outbox.setEventType("OrderCreated");
            outbox.setPayload(payload);
            outbox.setStatus(EventStatus.PENDING);

            return outbox;
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }
    }
}
