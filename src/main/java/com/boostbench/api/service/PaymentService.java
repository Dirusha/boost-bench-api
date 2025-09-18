package com.boostbench.api.service;

import com.boostbench.api.config.PayHereConfig;
import com.boostbench.api.dto.*;
import com.boostbench.api.entity.Order;
import com.boostbench.api.entity.OrderStatus;
import com.boostbench.api.entity.PaymentStatus;
import com.boostbench.api.entity.Product;
import com.boostbench.api.exception.ResourceNotFoundException;
import com.boostbench.api.repository.OrderRepository;
import com.boostbench.api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PayHereConfig payHereConfig;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    public PaymentInitiateResponse initiatePayment(Long orderId, Long userId, PaymentInitiateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not have permission to pay for this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Order has already been paid");
        }

        // Update customer details in the order
        order.setCustomerFirstName(request.getFirstName());
        order.setCustomerLastName(request.getLastName());
        order.setCustomerEmail(request.getEmail());
        order.setCustomerPhone(request.getPhone());
        order.setCustomerAddress(request.getAddress());
        order.setCustomerCity(request.getCity());
        order.setCustomerCountry(request.getCountry());
        orderRepository.save(order);

        String hash = generateHash(
                payHereConfig.getMerchantId(),
                orderId.toString(),
                order.getTotalAmount().toString(),
                "LKR"
        );

        String items = order.getItems().stream()
                .map(item -> item.getProduct().getName())
                .collect(Collectors.joining(", "));

        return PaymentInitiateResponse.builder()
                .merchantId(payHereConfig.getMerchantId())
                .orderId(orderId.toString())
                .amount(order.getTotalAmount())
                .currency("LKR")
                .hash(hash)
                .returnUrl(payHereConfig.getFrontendUrl() + "/payment/success")
                .cancelUrl(payHereConfig.getFrontendUrl() + "/payment/cancel")
//                .notifyUrl(payHereConfig.getBackendUrl() + "/api/payments/notify")
                .notifyUrl(payHereConfig.getNotifyUrl() + "/api/payments/notify")
                .sandbox(payHereConfig.isSandbox())
                .items(items.length() > 100 ? items.substring(0, 97) + "..." : items)
                .build();
    }

    @Transactional
    public void handlePaymentNotification(PaymentNotificationRequest notification) {
        System.out.println(notification);
        // Verify the hash for security
        String localHash = generateNotificationHash(notification);
        if (!localHash.equals(notification.getMd5sig())) {
            throw new SecurityException("Invalid payment notification hash");
        }

        Long orderId = Long.parseLong(notification.getOrder_id());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Handle different payment status codes
        switch (notification.getStatus_code()) {
            case "2": // Success
                // Update payment details
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaymentId(notification.getPayment_id());
                order.setPaymentMethod(notification.getMethod());
                order.setPaymentCompletedAt(LocalDateTime.now());
                order.setStatus(OrderStatus.CONFIRMED);

                // Reduce product quantities
                updateProductQuantities(order);
                break;

            case "0": // Pending
                order.setPaymentStatus(PaymentStatus.PENDING);
                break;

            case "-1": // Canceled
                order.setStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.CANCELLED);
                restoreProductQuantities(order);
                break;

            case "-2": // Failed
                order.setStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);
                restoreProductQuantities(order);
                break;

            default:
                throw new IllegalStateException("Unknown payment status: " + notification.getStatus_code());
        }

        orderRepository.save(order);
    }

    public PaymentStatusResponse getPaymentStatus(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not have permission to access this order");
        }

        return PaymentStatusResponse.builder()
                .orderId(orderId)
                .paymentStatus(order.getPaymentStatus())
                .paymentId(order.getPaymentId())
                .paymentMethod(order.getPaymentMethod())
                .message(getPaymentStatusMessage(order.getPaymentStatus()))
                .build();
    }

    private String getPaymentStatusMessage(PaymentStatus status) {
        switch (status) {
            case PENDING: return "Payment is pending";
            case PAID: return "Payment completed successfully";
            case FAILED: return "Payment failed";
            case CANCELLED: return "Payment was cancelled";
            case REFUNDED: return "Payment has been refunded";
            default: return "Unknown payment status";
        }
    }

    private void updateProductQuantities(Order order) {
        order.getItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();

            // Reduce available quantity and increase sold quantity
            int newAvailableQuantity = product.getAvailableQuantity() - orderItem.getQuantity();
            int newSoldQuantity = (product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + orderItem.getQuantity();

            if (newAvailableQuantity < 0) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            product.setAvailableQuantity(newAvailableQuantity);
            product.setSoldQuantity(newSoldQuantity);
            productRepository.save(product);
        });
    }

    private void restoreProductQuantities(Order order) {
        order.getItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();

            // Restore available quantity and reduce sold quantity (if payment was previously successful)
            product.setAvailableQuantity(product.getAvailableQuantity() + orderItem.getQuantity());
            int currentSoldQuantity = product.getSoldQuantity() != null ? product.getSoldQuantity() : 0;
            product.setSoldQuantity(Math.max(0, currentSoldQuantity - orderItem.getQuantity()));
            productRepository.save(product);
        });
    }

    private String generateHash(String merchantId, String orderId, String amount, String currency) {
        try {
            String merchantSecret = payHereConfig.getMerchantSecret();
            String hashedSecret = md5(merchantSecret).toUpperCase();
            String data = merchantId + orderId + amount + currency + hashedSecret;
            return md5(data).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error generating payment hash", e);
        }
    }

    private String generateNotificationHash(PaymentNotificationRequest notification) {
        try {
            String merchantSecret = payHereConfig.getMerchantSecret();
            String hashedSecret = md5(merchantSecret).toUpperCase();
            String data = notification.getMerchant_id() + notification.getOrder_id() +
                    notification.getPayhere_amount() + notification.getPayhere_currency() +
                    notification.getStatus_code() + hashedSecret;
            return md5(data).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error generating notification hash", e);
        }
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}