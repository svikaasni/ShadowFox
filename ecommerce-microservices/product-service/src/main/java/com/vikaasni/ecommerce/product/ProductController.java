package com.vikaasni.ecommerce.product;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> all() {
        return products.findByActiveTrue();
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable Long id) {
        return find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestHeader(value = "X-User-Role", required = false) String role,
                          @Valid @RequestBody ProductDtos.ProductRequest request) {
        requireAdmin(role);
        Product product = new Product();
        copy(request, product);
        return products.save(product);
    }

    @PutMapping("/{id}")
    public Product update(@RequestHeader(value = "X-User-Role", required = false) String role,
                          @PathVariable Long id,
                          @Valid @RequestBody ProductDtos.ProductRequest request) {
        requireAdmin(role);
        Product product = find(id);
        copy(request, product);
        return products.save(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "X-User-Role", required = false) String role,
                       @PathVariable Long id) {
        requireAdmin(role);
        Product product = find(id);
        product.setActive(false);
        products.save(product);
    }

    @PostMapping("/{id}/reserve")
    @Transactional
    public ProductDtos.ProductSnapshot reserve(@PathVariable Long id,
                                                @Valid @RequestBody ProductDtos.ReserveRequest request) {
        Product product = find(id);
        if (!product.isActive() || product.getStock() < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }
        product.setStock(product.getStock() - request.quantity());
        products.save(product);
        return new ProductDtos.ProductSnapshot(product.getId(), product.getName(),
                product.getPrice(), request.quantity());
    }

    private Product find(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void copy(ProductDtos.ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(true);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}
