package com.truongngo.catalog.domain.repository;

import com.truongngo.catalog.domain.model.PageResult;
import com.truongngo.catalog.domain.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    PageResult<Product> findPage(int page, int size);

    Product save(Product product);

    Optional<Product> findById(UUID id);

    void deleteById(UUID id);
}
