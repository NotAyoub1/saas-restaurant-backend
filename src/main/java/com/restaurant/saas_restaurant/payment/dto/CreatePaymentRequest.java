package com.restaurant.saas_restaurant.payment.dto;

import com.restaurant.saas_restaurant.restaurant.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull
    private PaymentMethod method;

    @Size(max = 100)
    private String reference; // optional, mainly for ONLINE
}