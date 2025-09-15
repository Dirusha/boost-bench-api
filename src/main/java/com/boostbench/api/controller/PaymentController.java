package com.boostbench.api.controller;

import com.boostbench.api.dto.PaymentInitiateRequest;
import com.boostbench.api.dto.PaymentInitiateResponse;
import com.boostbench.api.dto.PaymentNotificationRequest;
import com.boostbench.api.dto.PaymentStatusResponse;
import com.boostbench.api.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_PAY') or hasAuthority('ORDER_READ_OWN')")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @Valid @RequestBody PaymentInitiateRequest request) {
        System.out.println("Initiating payment");
        PaymentInitiateResponse response = paymentService.initiatePayment(orderId, userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handlePaymentNotification(@RequestParam Map<String, String> params) {
        try {
            System.out.println("Notifying payment");
            System.out.println(params);
            PaymentNotificationRequest notification = mapToNotificationRequest(params);
            paymentService.handlePaymentNotification(notification);
            return ResponseEntity.ok("OK");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("ERROR");
        }
    }

    @GetMapping("/status/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_READ_OWN') or hasAuthority('ORDER_READ')")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable Long orderId,
            @RequestParam Long userId) {

        PaymentStatusResponse response = paymentService.getPaymentStatus(orderId, userId);
        return ResponseEntity.ok(response);
    }

    private PaymentNotificationRequest mapToNotificationRequest(Map<String, String> params) {
        PaymentNotificationRequest notification = new PaymentNotificationRequest();
        notification.setMerchant_id(params.get("merchant_id"));
        notification.setOrder_id(params.get("order_id"));
        notification.setPayhere_amount(params.get("payhere_amount"));
        notification.setPayhere_currency(params.get("payhere_currency"));
        notification.setStatus_code(params.get("status_code"));
        notification.setMd5sig(params.get("md5sig"));
        notification.setCustom_1(params.get("custom_1"));
        notification.setCustom_2(params.get("custom_2"));
        notification.setMethod(params.get("method"));
        notification.setStatus_message(params.get("status_message"));
        notification.setCard_holder_name(params.get("card_holder_name"));
        notification.setCard_no(params.get("card_no"));
        notification.setCard_expiry(params.get("card_expiry"));
        notification.setPayment_id(params.get("payment_id"));
        return notification;
    }
}