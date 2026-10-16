package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.truongngo.catalog.domain.exception.ProductNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

class DeleteProductUseCaseTest {
    private ProductRepository productRepository;
    private DeleteProductUseCase deleteProductUseCase;

    @BeforeEach
    void setUp() {
        this.productRepository = mock(ProductRepository.class);
        this.deleteProductUseCase = new DeleteProductUseCase(productRepository);
    }

    @Test
    void deletesExistingProduct() {
        UUID id = UUID.randomUUID();

        Product product =
                new Product(
                        id,
                        "Microphone",
                        "This is test description",
                        new BigDecimal(50000),
                        Instant.now());

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        deleteProductUseCase.execute(id);

        verify(productRepository).deleteById(id);
    }

    @Test
    void doesNotDeleteWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> deleteProductUseCase.execute(id));

        verify(productRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void rejectsNullIdWithoutCallingRepository() {
        assertThrows(IllegalArgumentException.class, () -> deleteProductUseCase.execute(null));

        verifyNoInteractions(productRepository);
    }
}
