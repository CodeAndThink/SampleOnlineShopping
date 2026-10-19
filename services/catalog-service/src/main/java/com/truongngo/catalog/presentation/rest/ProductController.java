package com.truongngo.catalog.presentation.rest;

import com.truongngo.catalog.application.dto.CreateProductCommand;
import com.truongngo.catalog.application.dto.UpdateProductCommand;
import com.truongngo.catalog.application.usecase.*;
import com.truongngo.catalog.domain.model.Product;
import com.truongngo.catalog.presentation.rest.dto.CreateProductRequest;
import com.truongngo.catalog.presentation.rest.dto.PageResponse;
import com.truongngo.catalog.presentation.rest.dto.ProductResponse;
import com.truongngo.catalog.presentation.rest.dto.UpdateProductRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products") // đặt đường dẫn chung của controller
public class ProductController {

    private final GetProductsUseCase getProductsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductController(
            GetProductsUseCase getProductsUseCase,
            CreateProductUseCase createProductUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeleteProductUseCase deleteProductUseCase) {
        this.getProductsUseCase = getProductsUseCase;
        this.createProductUseCase = createProductUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @GetMapping // Nhận request http get
    public PageResponse<Product> getProducts(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        var result = getProductsUseCase.execute(keyword, page, size);

        return new PageResponse<>(
                result.items(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody CreateProductRequest request) {
        CreateProductCommand command =
                new CreateProductCommand(request.name(), request.description(), request.price());

        Product product = createProductUseCase.save(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return ProductResponse.from(getProductByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        UpdateProductCommand command =
                new UpdateProductCommand(request.name(), request.description(), request.price());

        Product updated = updateProductUseCase.execute(id, command);

        return ProductResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        deleteProductUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }
}
