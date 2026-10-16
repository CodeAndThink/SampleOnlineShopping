package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.application.dto.CreateProductCommand;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateProductUseCaseTest {
    private ProductRepository productRepository;
    private CreateProductUseCase createProductUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        createProductUseCase = new CreateProductUseCase(productRepository);
    }

    @Test
    void createsProductAndReturnsRepositoryResult() {
        CreateProductCommand command =
                new CreateProductCommand(
                        "  Keyboard  ", "Wireless keyboard", new BigDecimal("450000"));
        // Return a distinct value to check that the use case uses the repository result.
        Product savedProduct =
                new Product(
                        UUID.randomUUID(),
                        "Keyboard",
                        "Wireless keyboard",
                        new BigDecimal("450000.00"),
                        Instant.parse("2026-01-01T00:00:00Z"));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        Instant before = Instant.now();

        Product result = createProductUseCase.save(command);

        Instant after = Instant.now();
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product submitted = captor.getValue();
        assertAll(
                () -> assertSame(savedProduct, result),
                () -> assertNotNull(submitted.id()),
                () -> assertEquals("Keyboard", submitted.name()),
                () -> assertEquals(command.description(), submitted.description()),
                () -> assertEquals(new BigDecimal("450000.00"), submitted.price()),
                () -> assertNotNull(submitted.createdAt()),
                () -> assertFalse(submitted.createdAt().isBefore(before)),
                () -> assertFalse(submitted.createdAt().isAfter(after)));
        verifyNoMoreInteractions(productRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "12.34", "12.3400", "99999999999999999.99"})
    void acceptsValidPricesAndMaximumLengthName(String price) {
        String name = "A".repeat(255);
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result =
                createProductUseCase.save(
                        new CreateProductCommand(name, null, new BigDecimal(price)));

        assertEquals(name, result.name());
        assertNull(result.description());
        assertEquals(new BigDecimal(price).setScale(2), result.price());
        verify(productRepository).save(result);
    }

    @Test
    void rejectsNullCommandWithoutSaving() {
        assertRejected(null);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsMissingOrBlankNameWithoutSaving(String name) {
        assertRejected(new CreateProductCommand(name, null, BigDecimal.ONE));
    }

    @Test
    void rejectsNameLongerThan255CharactersWithoutSaving() {
        assertRejected(new CreateProductCommand("A".repeat(256), null, BigDecimal.ONE));
    }

    @Test
    void rejectsNullPriceWithoutSaving() {
        assertRejected(new CreateProductCommand("Keyboard", null, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-100", "12.345", "100000000000000000.00"})
    void rejectsInvalidPriceWithoutSaving(String price) {
        assertRejected(new CreateProductCommand("Keyboard", null, new BigDecimal(price)));
    }

    private void assertRejected(CreateProductCommand command) {
        assertThrows(IllegalArgumentException.class, () -> createProductUseCase.save(command));
        verifyNoInteractions(productRepository);
    }
}
