package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.application.dto.UpdateProductCommand;
import com.truongngo.catalog.domain.exception.ProductNotFoundException;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateProductUseCaseTest {

    private ProductRepository productRepository;
    private UpdateProductUseCase useCase;
    private Product existing;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        useCase = new UpdateProductUseCase(productRepository);

        existing = new Product(
                UUID.randomUUID(),
                "Old name",
                "Old description",
                new BigDecimal("100.00"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    @Test
    void updatesProductWhilePreservingIdAndCreatedAt() {
        when(productRepository.findById(existing.id()))
                .thenReturn(Optional.of(existing));

        Product savedProduct = new Product(
                existing.id(),
                "New name",
                null,
                new BigDecimal("250.00"),
                existing.createdAt()
        );

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        UpdateProductCommand command = new UpdateProductCommand(
                "  New name  ",
                null,
                new BigDecimal("250")
        );

        Product result = useCase.execute(existing.id(), command);

        ArgumentCaptor<Product> captor =
                ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(captor.capture());
        Product submitted = captor.getValue();

        assertAll(
                () -> assertEquals(existing.id(), submitted.id()),
                () -> assertEquals(existing.createdAt(), submitted.createdAt()),
                () -> assertEquals("New name", submitted.name()),
                () -> assertNull(submitted.description()),
                () -> assertEquals(new BigDecimal("250.00"), submitted.price()),
                () -> assertSame(savedProduct, result)
        );
    }

    @Test
    void doesNotSaveWhenProductDoesNotExist() {
        UUID missingId = UUID.randomUUID();

        when(productRepository.findById(missingId))
                .thenReturn(Optional.empty());

        UpdateProductCommand command = new UpdateProductCommand(
                "New name",
                null,
                new BigDecimal("250")
        );

        assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute(missingId, command)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void doesNotSaveWhenPriceIsNegative() {
        when(productRepository.findById(existing.id()))
                .thenReturn(Optional.of(existing));

        UpdateProductCommand command = new UpdateProductCommand(
                "New name",
                "New description",
                new BigDecimal("-1")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(existing.id(), command)
        );

        verify(productRepository, never()).save(any(Product.class));
    }
}