package com.boostbench.api.controller;

import com.boostbench.api.dto.OrderDto;
import com.boostbench.api.entity.OrderStatus;
import com.boostbench.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place/{userId}")
    @PreAuthorize("#userId == authentication.principal.id and hasAuthority('ORDER_CREATE')")
    public ResponseEntity<OrderDto> placeOrder(@PathVariable Long userId) {
        return new ResponseEntity<>(orderService.placeOrder(userId), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasAuthority('ORDER_READ_OWN')")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
    }

    // Fixed endpoint to match frontend Redux slice
    @GetMapping("/{orderId}/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasAuthority('ORDER_READ_OWN')")
    public ResponseEntity<OrderDto> getOrderByIdForUser(@PathVariable Long orderId, @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ORDER_READ_ALL')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ORDER_STATUS_UPDATE')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}