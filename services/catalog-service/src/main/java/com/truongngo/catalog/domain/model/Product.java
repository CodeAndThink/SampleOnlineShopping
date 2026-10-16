package com.truongngo.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Product(
        UUID id, String name, String description, BigDecimal price, Instant createdAt) {
    public Product {}
}
