package com.truongngo.catalog.presentation.rest.dto;

import java.math.BigDecimal;

public record CreateProductRequest (
        String name,
        String description,
        BigDecimal price
) {
}
