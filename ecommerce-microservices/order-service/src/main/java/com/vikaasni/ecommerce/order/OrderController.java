package com.vikaasni.ecommerce.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder create(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody OrderDtos.CreateOrderRequest request) {
        return service.create(userId, request);
    }

    @GetMapping("/my")
    public List<CustomerOrder> mine(@RequestHeader("X-User-Id") Long userId) {
        return service.mine(userId);
    }
}
