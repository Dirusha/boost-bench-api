package com.boostbench.api.dto;

import lombok.Data;

@Data
public class PaymentNotificationRequest {
    private String merchant_id;
    private String order_id;
    private String payhere_amount;
    private String payhere_currency;
    private String status_code;
    private String md5sig;
    private String custom_1;
    private String custom_2;
    private String method;
    private String status_message;
    private String card_holder_name;
    private String card_no;
    private String card_expiry;
    private String payment_id;
}