package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.domain.model.PageResult;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;

public class GetProductsUseCase {
    private final ProductRepository productRepository;

    public GetProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResult<Product> execute(String keyword, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to zero");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        String normalizeKeyword = keyword == null ? "" : keyword.strip();

        if (normalizeKeyword.isEmpty()) {
            return productRepository.findPage(page, size);
        }

        return productRepository.searchByName(normalizeKeyword, page, size);
    }
}
