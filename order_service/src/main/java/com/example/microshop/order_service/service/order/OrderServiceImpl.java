package com.example.microshop.order_service.service.order;

import com.example.microshop.order_service.domain.Order;
import com.example.microshop.order_service.domain.OrderFactory;
import com.example.microshop.order_service.domain.OrderItem;
import com.example.microshop.order_service.entity.OrderEntity;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.repository.OrderRepository;
import com.example.microshop.order_service.repository.OutboxRepository;
import com.example.microshop.order_service.service.OrderService;
import com.example.microshop.order_service.service.order.command.CreateOrderCommand;
import com.example.microshop.order_service.utils.OrderMapper;
import com.example.microshop.order_service.utils.OutboxBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реализация сервиса для управления заказами.
 * <p>
 * Обеспечивает создание заказа и позиций заказа, сохранение в базу данных,
 * а также генерацию outbox-события для дальнейшей отправки в шину сообщений (Kafka).
 * Работает в рамках одной транзакции (аннотация {@code @Transactional} на уровне класса).
 * </p>
 *
 * @see OrderService
 * @see OrderFactory
 * @see OutboxBuilder
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final OrderMapper orderMapper;
    private final OutboxBuilder outboxBuilder;

    /**
     * Создаёт новый заказ на основе команды.
     * <p>
     * Алгоритм работы:
     * <ol>
     *     <li>Создаёт доменный объект {@link Order} с использованием фабрики
     *         (генерируется случайный UUID и устанавливается идентификатор пользователя).</li>
     *     <li>Для каждой позиции из команды создаёт {@link OrderItem} через
     *         {@link OrderFactory#createOrderItem} и добавляет позицию к заказу.</li>
     *     <li>Преобразует доменный объект в сущность JPA через {@link OrderMapper}.</li>
     *     <li>Сохраняет сущность заказа в репозитории.</li>
     *     <li>Строит outbox-событие через {@link OutboxBuilder#buildOutboxEventFromOrderEntity}
     *         и сохраняет его в outbox-репозитории (событие будет отправлено позже планировщиком).</li>
     * </ol>
     * Вся операция выполняется атомарно в рамках одной транзакции.
     * </p>
     *
     * @param createOrderCommand команда, содержащая userId и список позиций заказа
     */
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

        OutboxEntity outboxEntity = outboxBuilder.buildOutboxEventFromOrderEntity(orderEntity);
        outboxRepository.save(outboxEntity);

    }
}
