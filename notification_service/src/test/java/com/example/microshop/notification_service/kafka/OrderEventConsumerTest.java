package com.example.microshop.notification_service.kafka;

import com.example.microshop.notification_service.kafka.event.OrderCreatedEvent;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
@Transactional
@EmbeddedKafka(partitions = 1, topics = {"orders"})
@DisplayName("Интеграционные тесты OrderEventConsumer с PostgreSQL и Kafka")
class OrderEventConsumerTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("internal.api.key", () -> "test-api-key");
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OrderRecordRepository orderRecordRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private BlockingQueue<ConsumerRecord<String, String>> records;

    private static final String TOPIC = "orders";

    @BeforeEach
    void setUp() {
        records = new LinkedBlockingQueue<>();
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    @DisplayName("Consumer успешно сохраняет заказ при корректном API-ключе")
    void shouldSaveOrderWhenApiKeyIsValid() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = createOrderCreatedEvent(orderId);
        String json = asJsonString(event);

        // ProducerRecord с заголовками
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, orderId.toString(), json);
        record.headers().add("X-Internal-Api-Key", "test-api-key".getBytes(StandardCharsets.UTF_8));
        record.headers().add("X-Trace-Id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);

        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo(orderId.toString());
        assertThat(received.value()).isEqualTo(json);

        // Время консьюмеру сохранить данные в БД
        Thread.sleep(2000);

        // Проверка
        var savedRecords = orderRecordRepository.findAll();
        assertThat(savedRecords).hasSize(1);
    }

    private OrderCreatedEvent createOrderCreatedEvent(UUID orderId) {
        OrderCreatedEvent.OrderItemEvent item = new OrderCreatedEvent.OrderItemEvent(
                UUID.randomUUID(), 2, 100.0, 10, 180.0);
        return new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                List.of(item),
                180.0,
                null
        );
    }

    private String asJsonString(Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }
}