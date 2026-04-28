package com.example.microshop.inventory_service.service.product;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.entity.ProductEntity;
import com.example.microshop.inventory_service.repository.ProductRepository;
import com.example.microshop.inventory_service.service.CreateProductCommand;
import com.example.microshop.inventory_service.service.ProductMapper;
import com.example.microshop.inventory_service.service.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ProductServiceImpl")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final UUID EXPECTED_PRODUCT_ID = UUID.randomUUID();
    private static final String EXPECTED_NAME = "Test Product";
    private static final Integer EXPECTED_QUANTITY = 100;
    private static final Double EXPECTED_PRICE = 49.99;
    private static final Integer EXPECTED_SALE = 10;
    private static final int REDUCE_BY = 30;
    private static final int EXPECTED_REDUCED = 30;

    @Test
    @DisplayName("findAll возвращает список продуктов")
    void shouldReturnAllProducts() {
        //given
        ProductEntity entity1 = new ProductEntity();
        ProductEntity entity2 = new ProductEntity();
        Product product1 = ProductFactory.createProduct(UUID.randomUUID(), "A", 10, 10.0, 0);
        Product product2 = ProductFactory.createProduct(UUID.randomUUID(), "B", 20, 20.0, 5);

        when(productRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(productMapper.fromEntity(entity1)).thenReturn(product1);
        when(productMapper.fromEntity(entity2)).thenReturn(product2);

        //when
        List<Product> result = productService.findAll();

        //then
        assertThat(result).hasSize(2).containsExactly(product1, product2);
        verify(productRepository).findAll();
        verify(productMapper).fromEntity(entity1);
        verify(productMapper).fromEntity(entity2);
    }

    @Test
    @DisplayName("findById возвращает продукт, если он существует")
    void shouldReturnProductWhenExists() {
        //given
        ProductEntity entity = new ProductEntity();
        Product product = ProductFactory.createProduct(EXPECTED_PRODUCT_ID, EXPECTED_NAME, EXPECTED_QUANTITY, EXPECTED_PRICE, EXPECTED_SALE);

        when(productRepository.findById(EXPECTED_PRODUCT_ID)).thenReturn(Optional.of(entity));
        when(productMapper.fromEntity(entity)).thenReturn(product);

        //when
        Product result = productService.findById(EXPECTED_PRODUCT_ID);

        //then
        assertThat(result).isEqualTo(product);
        verify(productRepository).findById(EXPECTED_PRODUCT_ID);
        verify(productMapper).fromEntity(entity);
    }

    @Test
    @DisplayName("findById выбрасывает EntityNotFoundException, если продукт не найден")
    void shouldThrowNotFoundExceptionWhenProductMissing() {
        //given
        when(productRepository.findById(EXPECTED_PRODUCT_ID)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> productService.findById(EXPECTED_PRODUCT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(EXPECTED_PRODUCT_ID.toString());
        verify(productRepository).findById(EXPECTED_PRODUCT_ID);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("createProduct успешно создаёт продукт")
    void shouldCreateProduct() {
        //given
        CreateProductCommand command = new CreateProductCommand(EXPECTED_NAME, EXPECTED_QUANTITY, EXPECTED_PRICE, EXPECTED_SALE);
        Product product = ProductFactory.createProduct(EXPECTED_PRODUCT_ID, EXPECTED_NAME, EXPECTED_QUANTITY, EXPECTED_PRICE, EXPECTED_SALE);
        ProductEntity entity = new ProductEntity();

        when(productMapper.toEntity(any(Product.class))).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);

        //when
        productService.createProduct(command);

        //then
        verify(productMapper).toEntity(any(Product.class));
        verify(productRepository).save(entity);
    }

    @Test
    @DisplayName("deleteProduct вызывает deleteById репозитория")
    void shouldDeleteProductById() {
        //when
        productService.deleteProduct(EXPECTED_PRODUCT_ID);

        //then
        verify(productRepository).deleteById(EXPECTED_PRODUCT_ID);
    }

    @Test
    @DisplayName("reduceProductQuantity успешно уменьшает количество")
    void shouldReduceQuantitySuccessfully() {
        //given
        ProductEntity entity = new ProductEntity();
        Product product = ProductFactory.createProduct(EXPECTED_PRODUCT_ID, EXPECTED_NAME, EXPECTED_QUANTITY, EXPECTED_PRICE, EXPECTED_SALE);

        when(productRepository.findById(EXPECTED_PRODUCT_ID)).thenReturn(Optional.of(entity));
        when(productMapper.fromEntity(entity)).thenReturn(product);
        when(productRepository.save(entity)).thenReturn(entity);

        //when
        int result = productService.reduceProductQuantity(EXPECTED_PRODUCT_ID, REDUCE_BY);

        //then
        assertThat(result).isEqualTo(EXPECTED_REDUCED);
        verify(productRepository).findById(EXPECTED_PRODUCT_ID);
        verify(productMapper).fromEntity(entity);
        verify(productRepository).save(entity);
    }

    @Test
    @DisplayName("reduceProductQuantity выбрасывает EntityNotFoundException, если продукт не найден")
    void shouldThrowExceptionWhenReducingMissingProduct() {
        //given
        when(productRepository.findById(EXPECTED_PRODUCT_ID)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> productService.reduceProductQuantity(EXPECTED_PRODUCT_ID, REDUCE_BY))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(EXPECTED_PRODUCT_ID.toString());
        verify(productRepository).findById(EXPECTED_PRODUCT_ID);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }
}
