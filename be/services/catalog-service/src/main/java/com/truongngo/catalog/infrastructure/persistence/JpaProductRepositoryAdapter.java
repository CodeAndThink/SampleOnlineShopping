package com.truongngo.catalog.infrastructure.persistence;

import com.truongngo.catalog.domain.model.PageResult;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository jpaRepository;

    public JpaProductRepositoryAdapter(SpringDataProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PageResult<Product> findPage(int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        var result = jpaRepository.findAll(pageRequest);

        return new PageResult<>(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity =
                new ProductJpaEntity(
                        product.id(),
                        product.name(),
                        product.description(),
                        product.price(),
                        product.createdAt());

        ProductJpaEntity savedEntity = jpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public PageResult<Product> searchByName(String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        var result = jpaRepository.findByNameContainingIgnoreCase(keyword, pageRequest);

        return new PageResult<>(
          result.getContent().stream().map(this::toDomain).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }

    private Product toDomain(ProductJpaEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCreatedAt());
    }
}
