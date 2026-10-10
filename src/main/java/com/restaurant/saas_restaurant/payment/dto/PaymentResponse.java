package com.restaurant.saas_restaurant.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.restaurant.saas_restaurant.restaurant.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long restaurantId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String reference;
    private String invoiceNumber;
    private Long paidBy;
    private LocalDateTime paidAt;
}