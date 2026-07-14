package com.vikaasni.ecommerce.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {
    @PostMapping("/{id}/reserve")
    OrderDtos.ProductSnapshot reserve(@PathVariable("id") Long id, @RequestBody OrderDtos.ReserveRequest request);
}
