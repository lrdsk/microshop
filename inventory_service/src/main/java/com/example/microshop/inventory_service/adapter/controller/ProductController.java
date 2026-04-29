package com.example.microshop.inventory_service.adapter.controller;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.dto.ProductRequestDTO;
import com.example.microshop.inventory_service.dto.ProductResponseDTO;
import com.example.microshop.inventory_service.service.CreateProductCommand;
import com.example.microshop.inventory_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для управления товарами в инвентаре.
 * <p>
 * Предоставляет эндпоинты для:
 * <ul>
 *     <li>получения списка всех товаров;</li>
 *     <li>получения товара по идентификатору;</li>
 *     <li>создания нового товара;</li>
 *     <li>удаления товара по идентификатору.</li>
 * </ul>
 * </p>
 * <p>
 * Базовый путь: {@code /api/products}.
 * </p>
 *
 * @see ProductService
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    /**
     * Получение списка всех товаров.
     *
     * @return список товаров в виде {@link ProductResponseDTO}
     */
    @GetMapping
    public List<ProductResponseDTO> getAllProducts() {
        log.info("REST request to find all products from inventory");
        List<Product> products = productService.findAll();
        return mapToProductResponseDTOs(products);
    }

    /**
     * Получение товара по его уникальному идентификатору.
     *
     * @param id идентификатор товара (UUID)
     * @return DTO с данными товара
     * @throws jakarta.persistence.EntityNotFoundException если товар не найден
     */
    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable("id") UUID id) {
        log.info("REST request to find product by id: {}", id);
        Product product = productService.findById(id);
        return mapToProductResponseDTO(product);
    }

    /**
     * Создание нового товара.
     *
     * @param productRequestDTO DTO с данными для создания товара (название, количество, цена, скидка)
     * @return ответ со статусом {@code 201 CREATED} при успешном создании
     */
    @PostMapping
    public ResponseEntity<HttpStatus> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        log.info("REST request to create product: {}", productRequestDTO);
        CreateProductCommand command = new CreateProductCommand(
                productRequestDTO.name(),
                productRequestDTO.quantity(),
                productRequestDTO.price(),
                productRequestDTO.sale()
        );
        productService.createProduct(command);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Удаление товара по его идентификатору.
     *
     * @param id идентификатор товара
     * @return ответ со статусом {@code 200 OK} при успешном удалении
     * @throws jakarta.persistence.EntityNotFoundException если товар не найден
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteProductById(@PathVariable("id") UUID id) {
        log.info("REST request to delete product by id: {}", id);
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private static List<ProductResponseDTO> mapToProductResponseDTOs(List<Product> products) {
        return products.stream()
                .map(ProductController::mapToProductResponseDTO)
                .toList();
    }

    private static ProductResponseDTO mapToProductResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                product.getSale(),
                product.getCreatedAt(),
                product.getModifiedAt()
        );
    }
}
