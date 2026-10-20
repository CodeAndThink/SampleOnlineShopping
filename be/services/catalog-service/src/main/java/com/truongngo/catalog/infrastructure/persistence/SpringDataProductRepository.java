package com.truongngo.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {
    Page<ProductJpaEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
