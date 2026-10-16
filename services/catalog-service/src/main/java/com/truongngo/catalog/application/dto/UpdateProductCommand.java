package com.truongngo.catalog.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateProductCommand (String name, String description, BigDecimal price) {
}
