package com.example.microshop.inventory_service.service;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.entity.ProductEntity;
import com.example.microshop.inventory_service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
    public int reduceProductQuantity(UUID id, int quantity) {
        log.info("Called ProductService to reduce product quantity for product with id: {} and reduce on quantity: {}", id, quantity);
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id '%s' not found".formatted(id)));

        Product product = productMapper.fromEntity(productEntity);
        int reducedQuantity = product.reduceQuantity(quantity);

        log.info("Product quantity reduced on: {}", reducedQuantity);

        ProductEntity productEntityAfterReduceQuantity = productMapper.toEntity(product);
        productRepository.save(productEntityAfterReduceQuantity);

        return reducedQuantity;
    }
}
