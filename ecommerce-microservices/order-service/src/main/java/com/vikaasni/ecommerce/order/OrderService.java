package com.vikaasni.ecommerce.order;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductClient productClient;

    public OrderService(OrderRepository orders, ProductClient productClient) {
        this.orders = orders;
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "productUnavailable")
    public CustomerOrder create(Long userId, OrderDtos.CreateOrderRequest request) {
        OrderDtos.ProductSnapshot product =
                productClient.reserve(request.productId(), new OrderDtos.ReserveRequest(request.quantity()));

        CustomerOrder order = new CustomerOrder();
        order.setUserId(userId);
        order.setProductId(product.productId());
        order.setProductName(product.name());
        order.setQuantity(request.quantity());
        order.setUnitPrice(product.unitPrice());
        order.setTotalPrice(product.unitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        order.setStatus(CustomerOrder.Status.CONFIRMED);
        return orders.save(order);
    }

    public CustomerOrder productUnavailable(Long userId, OrderDtos.CreateOrderRequest request, Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Product service is unavailable or the product cannot be reserved");
    }

    public List<CustomerOrder> mine(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
