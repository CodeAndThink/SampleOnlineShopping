package com.truongngo.catalog.presentation.rest.dto;

import com.truongngo.catalog.domain.model.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id, String name, String description, BigDecimal price, Instant createdAt) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.description(),
                product.price(),
                product.createdAt());
    }
}
