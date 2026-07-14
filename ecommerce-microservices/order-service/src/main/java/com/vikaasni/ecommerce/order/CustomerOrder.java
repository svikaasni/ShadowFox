package com.vikaasni.ecommerce.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private Long productId;
    @Column(nullable = false) private String productName;
    @Column(nullable = false) private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(nullable = false) private LocalDateTime createdAt;

    public enum Status { CREATED, CONFIRMED, CANCELLED }

    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
