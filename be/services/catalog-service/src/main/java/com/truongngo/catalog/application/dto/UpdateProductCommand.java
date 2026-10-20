package com.truongngo.catalog.application.dto;

import java.math.BigDecimal;

public record UpdateProductCommand(String name, String description, BigDecimal price) {
}
