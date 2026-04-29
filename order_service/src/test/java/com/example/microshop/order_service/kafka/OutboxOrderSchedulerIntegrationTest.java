package com.example.microshop.order_service.kafka;

import com.example.microshop.order_service.entity.EventStatus;
import com.example.microshop.order_service.entity.OutboxEntity;
import com.example.microshop.order_service.kafka.scheduler.OutboxOrderScheduler;
import com.example.microshop.order_service.repository.OutboxRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
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
@DisplayName("Интеграционные тесты OutboxOrderScheduler с PostgreSQL и Kafka")
class OutboxOrderSchedulerIntegrationTest {

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
    }

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OutboxOrderScheduler outboxOrderScheduler;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private BlockingQueue<ConsumerRecord<String, String>> records;

    @BeforeEach
    void setUp() {
        records = new LinkedBlockingQueue<>();
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("orders");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    @DisplayName("Планировщик отправляет PENDING событие в Kafka и обновляет статус")
    void shouldSendPendingEventToKafka() throws Exception {
        OutboxEntity event = new OutboxEntity();
        event.setAggregateId(UUID.randomUUID());
        event.setEventType("OrderCreated");
        event.setPayload("{\"orderId\":\"123\"}");
        event.setStatus(EventStatus.PENDING);
        event.setTraceId(UUID.randomUUID());
        outboxRepository.save(event);

        outboxOrderScheduler.processPendingEvents();

        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo("orders");
        assertThat(received.key()).isEqualTo(event.getAggregateId().toString());
        assertThat(received.value()).isEqualTo(event.getPayload());

        assertThat(received.headers().lastHeader("X-Internal-Api-Key")).isNotNull();
        assertThat(new String(received.headers().lastHeader("X-Internal-Api-Key").value()))
                .isEqualTo("secret-api-key");

        assertThat(received.headers().lastHeader("X-Trace-Id")).isNotNull();
        assertThat(new String(received.headers().lastHeader("X-Trace-Id").value()))
                .isEqualTo(event.getTraceId().toString());

        OutboxEntity updated = outboxRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EventStatus.SENT);
        assertThat(updated.getSentAt()).isNotNull();
    }
}