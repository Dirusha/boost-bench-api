package com.boostbench.api.dto;

import com.boostbench.api.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusResponse {
    private Long orderId;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private String paymentMethod;
    private String message;
}