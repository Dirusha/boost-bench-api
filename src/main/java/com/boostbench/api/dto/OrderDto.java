package com.boostbench.api.dto;

import com.boostbench.api.entity.OrderStatus;
import com.boostbench.api.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private String paymentMethod;
    private LocalDateTime paymentCompletedAt;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerCity;
    private String customerCountry;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}