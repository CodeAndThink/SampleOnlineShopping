package com.truongngo.catalog.application.usecase;

import com.truongngo.catalog.domain.model.PageResult;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class GetProductsUseCaseTest {
    private ProductRepository productRepository;
    private GetProductsUseCase getProductsUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        getProductsUseCase = new GetProductsUseCase(productRepository);
    }

    @Test
    void returnPageFromRepository() {
        PageResult<Product> expected = new PageResult<>(
                List.of(), 0, 20, 0L, 0
        );

        when(productRepository.findPage(0, 20)).thenReturn(expected);

        PageResult<Product> result = getProductsUseCase.execute(null, 0, 20);

        assertSame(expected, result);
        verify(productRepository).findPage(0, 20);
    }

    @Test
    void rejectsNegativePageWithoutCallingRepository() {
        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsUseCase.execute(null, -1, 20)
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsZeroSizeWithoutCallingRepository() {
        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsUseCase.execute(null, 0, 0));

        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsOutOfRangeOfSizeWithoutCallingRepository() {
        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsUseCase.execute(null, 0, 101));

        verifyNoInteractions(productRepository);
    }

    @Test
    void searchesByTrimmedKeyword() {
        PageResult<Product> expected = new PageResult<>(
                List.of(), 0, 20, 0L, 0
        );

        when(productRepository.searchByName("mouse", 0, 20))
                .thenReturn(expected);

        PageResult<Product> result =
                getProductsUseCase.execute("  mouse  ", 0, 20);

        assertSame(expected, result);
        verify(productRepository).searchByName("mouse", 0, 20);
        verifyNoMoreInteractions(productRepository);
    }
}
