package com.truongngo.catalog.domain.repository;

import com.truongngo.catalog.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    List<Product> findAll();

    Product save(Product product);

    Optional<Product> findById(UUID id);

    void deleteById(UUID id);
}
