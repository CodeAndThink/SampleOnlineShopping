package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.application.dto.UpdateProductCommand;
import com.truongngo.catalog.domain.exception.ProductNotFoundException;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public class UpdateProductUseCase {
    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(UUID id, UpdateProductCommand command) {
        if (id == null || command == null) {
            throw new IllegalArgumentException(
                    "Product ID and update information are required."
            );
        }

        Product existing = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        if(command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Product name must not be blank."
            );
        }

        String name = command.name().strip();

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Product name must not exceed 255 characters."
            );
        }

        if (command.price() == null || command.price().signum() < 0) {
            throw new IllegalArgumentException(
                    "Product price must be non-negative."
            );
        }

        BigDecimal price;
        try {
            price = command.price().setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Product price may have a maximum of two decimal places."
            );
        }

        if (price.precision() > 19) {
            throw new IllegalArgumentException(
                    "Product price exceeds storage limit."
            );
        }

        Product updated = new Product(
                existing.id(),
                name,
                command.description(),
                price,
                existing.createdAt()
        );

        return productRepository.save(updated);
    }
}
