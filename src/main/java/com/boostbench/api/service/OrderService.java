package com.boostbench.api.service;

import com.boostbench.api.dto.OrderDto;
import com.boostbench.api.entity.OrderStatus;
import com.boostbench.api.entity.PaymentStatus;

import java.util.List;

public interface OrderService {

    OrderDto placeOrder(Long userId);

    List<OrderDto> getUserOrders(Long userId);

    OrderDto getOrderById(Long orderId, Long userId);

    List<OrderDto> getAllOrders();

    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);

    OrderDto updateOrderPaymentStatus(Long orderId, PaymentStatus paymentStatus, String paymentId, String paymentMethod);
}