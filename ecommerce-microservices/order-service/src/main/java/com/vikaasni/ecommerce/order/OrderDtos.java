package com.vikaasni.ecommerce.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class OrderDtos {
    private OrderDtos() {}
    public record CreateOrderRequest(@NotNull Long productId, @NotNull @Min(1) Integer quantity) {}
    public record ReserveRequest(Integer quantity) {}
    public record ProductSnapshot(Long productId, String name, BigDecimal unitPrice, Integer reservedQuantity) {}
}
