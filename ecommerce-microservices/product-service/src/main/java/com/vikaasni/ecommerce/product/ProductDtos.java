package com.vikaasni.ecommerce.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class ProductDtos {
    private ProductDtos() {}

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull @Min(0) Integer stock
    ) {}

    public record ReserveRequest(@NotNull @Min(1) Integer quantity) {}
    public record ProductSnapshot(Long productId, String name, BigDecimal unitPrice, Integer reservedQuantity) {}
}
