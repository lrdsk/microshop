package com.example.microshop.inventory_service.service;

import com.example.microshop.inventory_service.domain.Product;
import com.example.microshop.inventory_service.domain.ProductFactory;
import com.example.microshop.inventory_service.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product fromEntity(ProductEntity productEntity) {
        Product product = ProductFactory.createProduct(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getQuantity(),
                productEntity.getPrice(),
                productEntity.getSale()
        );
        ProductFactory.setAudit(product, productEntity.getCreatedAt(), productEntity.getModifiedAt());
        return product;
    }

    public ProductEntity toEntity(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(product.getId());
        productEntity.setName(product.getName());
        productEntity.setQuantity(product.getQuantity());
        productEntity.setPrice(product.getPrice());
        productEntity.setSale(productEntity.getSale());

        return productEntity;
    }
}
