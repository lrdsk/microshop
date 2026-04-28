package com.example.microshop.notification_service.service;

import com.example.microshop.notification_service.dto.OrderRecordResponseDTO;
import com.example.microshop.notification_service.entity.OrderRecordEntity;
import com.example.microshop.notification_service.repository.OrderRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для SearchOrderRecordServiceImpl")
class SearchOrderRecordServiceImplTest {

    @Mock
    private OrderRecordRepository orderRecordRepository;

    @InjectMocks
    private SearchOrderRecordServiceImpl searchService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Test
    @DisplayName("findAll возвращает список DTO, преобразованных из всех записей")
    void findAllShouldReturnAllRecordsAsDTO() {
        // given
        OrderRecordEntity entity1 = createOrderRecordEntity(USER_ID, ORDER_ID);
        OrderRecordEntity entity2 = createOrderRecordEntity(USER_ID, ORDER_ID);
        when(orderRecordRepository.findAll()).thenReturn(List.of(entity1, entity2));

        // when
        List<OrderRecordResponseDTO> result = searchService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderId()).isEqualTo(ORDER_ID);
        assertThat(result.get(1).orderId()).isEqualTo(ORDER_ID);
        verify(orderRecordRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findByOrderId возвращает DTO для записей с указанным orderId")
    void findByOrderIdShouldReturnMatchingRecords() {
        // given
        OrderRecordEntity entity = createOrderRecordEntity(USER_ID, ORDER_ID);
        when(orderRecordRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(entity));

        // when
        List<OrderRecordResponseDTO> result = searchService.findByOrderId(ORDER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).orderId()).isEqualTo(ORDER_ID);
        assertThat(result.get(0).userId()).isEqualTo(USER_ID);
        verify(orderRecordRepository).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("findByOrderId возвращает пустой список, если записей нет")
    void findByOrderIdShouldReturnEmptyListWhenNoneFound() {
        when(orderRecordRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        List<OrderRecordResponseDTO> result = searchService.findByOrderId(ORDER_ID);

        assertThat(result).isEmpty();
        verify(orderRecordRepository).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("findByUserId возвращает DTO для записей с указанным userId")
    void findByUserIdShouldReturnMatchingRecords() {
        // given
        OrderRecordEntity entity = createOrderRecordEntity(USER_ID, ORDER_ID);
        when(orderRecordRepository.findByUserId(USER_ID)).thenReturn(List.of(entity));

        // when
        List<OrderRecordResponseDTO> result = searchService.findByUserId(USER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(USER_ID);
        verify(orderRecordRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("findByUserId возвращает пустой список, если записей нет")
    void findByUserIdShouldReturnEmptyListWhenNoneFound() {
        when(orderRecordRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<OrderRecordResponseDTO> result = searchService.findByUserId(USER_ID);

        assertThat(result).isEmpty();
        verify(orderRecordRepository).findByUserId(USER_ID);
    }

    // Вспомогательный метод для создания тестовой сущности
    private OrderRecordEntity createOrderRecordEntity(UUID userId, UUID orderId) {
        OrderRecordEntity entity = new OrderRecordEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrderId(orderId);
        entity.setProductId(PRODUCT_ID);
        entity.setQuantity(2);
        entity.setPrice(BigDecimal.valueOf(100.0));
        entity.setSale(10);
        entity.setTotalPrice(BigDecimal.valueOf(180.0));
        entity.setUserId(userId);
        return entity;
    }
}
