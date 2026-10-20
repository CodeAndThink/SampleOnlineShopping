package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.domain.exception.ProductNotFoundException;
import com.truongngo.catalog.domain.repository.ProductRepository;

import java.util.UUID;

public class DeleteProductUseCase {
    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void execute(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }

        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.deleteById(id);
    }
}
