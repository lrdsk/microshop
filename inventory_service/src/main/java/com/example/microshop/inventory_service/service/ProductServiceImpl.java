package com.example.microshop.inventory_service.service;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.entity.ProductEntity;
import com.example.microshop.inventory_service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления товарами в инвентаре.
 * <p>
 * Предоставляет методы для:
 * <ul>
 *     <li>получения всех товаров или по идентификаторам;</li>
 *     <li>создания нового товара;</li>
 *     <li>удаления товара;</li>
 *     <li>массового списания остатков с пессимистической блокировкой.</li>
 * </ul>
 * </p>
 *
 * @see ProductService
 * @see ProductRepository
 * @see ProductMapper
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<Product> findAll() {
        log.info("Called ProductService to find all products");
        return productRepository.findAll()
                .stream()
                .map(productMapper::fromEntity)
                .toList();
    }

    @Override
    public Product findById(UUID id) {
        log.info("Called ProductService to find product by id");
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id '%s' not found".formatted(id)));

        return productMapper.fromEntity(productEntity);
    }

    @Override
    public void createProduct(CreateProductCommand command) {
        log.info("Called ProductService to create new product: {}", command);

        Product product = ProductFactory.createProduct(UUID.randomUUID(), command.name(), command.quantity(), command.price(), command.sale());
        ProductEntity entity = productMapper.toEntity(product);
        productRepository.save(entity);

        log.info("ProductService has been created new product successfully: {}", product);
    }

    @Override
    public void deleteProduct(UUID id) {
        log.info("Called ProductService to delete product by id: {}", id);
        productRepository.deleteById(id);
    }

    @Override
    public Map<UUID, Product> findAllByIds(List<UUID> productIds) {
        log.info("Called ProductService to find products in ids: {}", productIds);
        List<Product> products = productRepository.findAllById(productIds)
                .stream()
                .map(productMapper::fromEntity)
                .toList();
        return products.stream().collect(Collectors.toMap(Product::getId, p -> p));
    }

    @Override
    @Transactional
    public Map<UUID, Product> batchReduceQuantities(Map<UUID, Integer> requiredQuantities) {
        log.info("Called ProductService to reduce product quantity for products: {}", requiredQuantities);

        List<UUID> ids = new ArrayList<>(requiredQuantities.keySet());
        List<ProductEntity> productEntities = productRepository.findAllByIdWithPessimisticLock(ids);
        List<Product> products = productEntities.stream()
                .map(productMapper::fromEntity)
                .toList();

        Map<UUID, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            Integer reduceBy = requiredQuantities.get(product.getId());
            if (reduceBy == null) continue;

            int reducedQuantity = product.reduceQuantity(reduceBy);
            productEntities.get(i).setQuantity(reducedQuantity);
            log.info("Product quantity reduced on: {} for product: {}", reducedQuantity, product.getId());
        }

        productRepository.saveAll(productEntities);

        return productMap;
    }
}
