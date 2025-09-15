package com.boostbench.api.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentInitiateResponse {
    private String merchantId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String hash;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;
    private boolean sandbox;
    private String items;
}