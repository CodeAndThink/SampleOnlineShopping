package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.application.dto.CreateProductCommand;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public class CreateProductUseCase {
    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(CreateProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Information of product cannot be null.");
        }

        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Information of product name cannot be null.");
        }

        String name = command.name().strip(); // Loại bỏ khoảng trắng đầu và cuối của tên

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Information of product name cannot be longer than 255.");
        }

        if (command.price() == null || command.price().signum() < 0) {
            throw new IllegalArgumentException(
                    "Information of product price has to be non-negative.");
        }

        BigDecimal price;

        try {
            // Giữ 2 số thập phân, từ chối giá cần làm tròn
            price = command.price().setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "The product price may have a maximum of two decimal places.");
        }

        if (price.precision() > 19) {
            throw new IllegalArgumentException("Product price exceeds storage limit.");
        }

        Product product =
                new Product(UUID.randomUUID(), name, command.description(), price, Instant.now());

        return productRepository.save(product);
    }
}
