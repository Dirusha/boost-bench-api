package com.boostbench.api.service;

import com.boostbench.api.dto.CartDto;
import com.boostbench.api.dto.OrderDto;
import com.boostbench.api.dto.OrderItemDto;
import com.boostbench.api.entity.*;
import com.boostbench.api.exception.ResourceNotFoundException;
import com.boostbench.api.repository.OrderItemRepository;
import com.boostbench.api.repository.OrderRepository;
import com.boostbench.api.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDto placeOrder(Long userId) {
        CartDto cartDto = cartService.getCartByUserId(userId);
        if (cartDto.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order: Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        List<OrderItem> orderItems = cartDto.getItems().stream()
                .map(cartItemDto -> {
                    Product product = productRepository.findById(cartItemDto.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartItemDto.getProductId()));

                    if (product.getAvailableQuantity() < cartItemDto.getQuantity()) {
                        throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                    }

                    BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
                    BigDecimal itemPrice = product.getPrice().subtract(discount);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(cartItemDto.getQuantity());
                    orderItem.setPrice(itemPrice);

                    order.setTotalAmount(order.getTotalAmount().add(itemPrice.multiply(BigDecimal.valueOf(cartItemDto.getQuantity()))));

                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(userId);

        return convertToDto(savedOrder);
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not have permission to access this order");
        }
        return convertToDto(order);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto updateOrderPaymentStatus(Long orderId, PaymentStatus paymentStatus, String paymentId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setPaymentStatus(paymentStatus);
        order.setPaymentId(paymentId);
        order.setPaymentMethod(paymentMethod);

        if (paymentStatus == PaymentStatus.PAID) {
            order.setPaymentCompletedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CONFIRMED);

            // Update product quantities
            for (OrderItem orderItem : order.getItems()) {
                Product product = orderItem.getProduct();
                int orderedQuantity = orderItem.getQuantity();

                // Update availableQuantity and soldQuantity
                product.setAvailableQuantity(product.getAvailableQuantity() - orderedQuantity);
                product.setSoldQuantity(product.getSoldQuantity() + orderedQuantity);

                // Validate available quantity
                if (product.getAvailableQuantity() < 0) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }

                // Save the updated product
                productRepository.save(product);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    private OrderDto convertToDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setItems(order.getItems().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList()));
        return orderDto;
    }

    private OrderItemDto convertToItemDto(OrderItem orderItem) {
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setId(orderItem.getId());
        itemDto.setProductId(orderItem.getProduct().getId());
        itemDto.setProductName(orderItem.getProduct().getName());
        itemDto.setQuantity(orderItem.getQuantity());
        itemDto.setPrice(orderItem.getPrice());
        return itemDto;
    }
}