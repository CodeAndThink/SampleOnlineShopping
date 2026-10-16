package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.exception.ProductNotFoundException;
import com.truongngo.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetProductByIdUseCaseTest {
    private ProductRepository productRepository;
    private GetProductByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        this.productRepository = mock(ProductRepository.class);
        useCase = new GetProductByIdUseCase(productRepository);
    }

    @Test
    void returnsProductWhenFound() {
        UUID id = UUID.randomUUID();

        Product product = new Product(
                id,
                "Bàn phím",
                "Bàn phím cơ",
                new BigDecimal("450000.00"),
                Instant.parse("2026-01-01T00:00:00Z")
        );

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = useCase.execute(id);

        assertEquals(product, result);
    }

    @Test
    void throwsExceptionWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(id));
    }
}
