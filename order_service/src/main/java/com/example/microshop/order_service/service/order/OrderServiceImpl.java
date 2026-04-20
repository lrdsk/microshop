package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.domain.Order;
import com.example.microshop.order_service.domain.OrderFactory;
import com.example.microshop.order_service.domain.OrderItem;
import com.example.microshop.order_service.domain.User;
import com.example.microshop.order_service.repository.OrderRepository;
import com.example.microshop.order_service.service.OrderService;
import com.example.microshop.order_service.service.UserService;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;

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

        //todo: Дописать сервис, чтобы сохранять order и его items в БД и написать 2-ой сервис, который будет сохранять
        //todo: новый event создания order, сохранять в event_outbox
    }
}
