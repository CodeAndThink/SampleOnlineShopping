package com.truongngo.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProductRepository
    extends JpaRepository<ProductJpaEntity, UUID> {
}