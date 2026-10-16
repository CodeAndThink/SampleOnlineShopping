package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.exception.ProductNotFoundException;
import com.truongngo.catalog.domain.repository.ProductRepository;

import java.util.UUID;

public class GetProductByIdUseCase {
    private final ProductRepository productRepository;

    public GetProductByIdUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
