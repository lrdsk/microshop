package com.example.microshop.order_service.microshop.inventory_service.adapter.controller;

import com.example.microshop.order_service.microshop.inventory_service.domain.Product;
import com.example.microshop.order_service.microshop.inventory_service.dto.ProductRequestDTO;
import com.example.microshop.order_service.microshop.inventory_service.dto.ProductResponseDTO;
import com.example.microshop.order_service.microshop.inventory_service.service.CreateProductCommand;
import com.example.microshop.order_service.microshop.inventory_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public List<ProductResponseDTO> getAllProducts() {
        log.info("REST request to find all products from inventory");
        List<Product> products = productService.findAll();
        return mapToProductResponseDTOs(products);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable("id") UUID id) {
        log.info("REST request to find product by id: {}", id);
        Product product = productService.findById(id);
        return mapToProductResponseDTO(product);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        log.info("REST request to create product: {}", productRequestDTO);
        CreateProductCommand command = new CreateProductCommand(
                productRequestDTO.id(),
                productRequestDTO.name(),
                productRequestDTO.quantity(),
                productRequestDTO.price(),
                productRequestDTO.sale()
        );
        productService.createProduct(command);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

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
