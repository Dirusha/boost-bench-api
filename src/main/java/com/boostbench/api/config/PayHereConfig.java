package com.boostbench.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Component
@Getter
public class PayHereConfig {

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    @Value("${payhere.sandbox:true}")
    private boolean sandbox;

    @Value("${payhere.sandbox.url:https://sandbox.payhere.lk/pay/checkout}")
    private String sandboxUrl;

    @Value("${payhere.production.url:https://www.payhere.lk/pay/checkout}")
    private String productionUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.backend.url}")
    private String backendUrl;

    public String getPaymentUrl() {
        return sandbox ? sandboxUrl : productionUrl;
    }
}