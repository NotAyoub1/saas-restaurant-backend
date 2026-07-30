package com.restaurant.saas_restaurant.restaurant.dto;

import java.util.Set;

import com.restaurant.saas_restaurant.restaurant.enums.PaymentMethod;
import com.restaurant.saas_restaurant.restaurant.enums.ServiceType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRestaurantRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone is required")
    private String phone;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @NotEmpty(message = "At least one payment method is required")
    private Set<PaymentMethod> acceptedPaymentMethods;

    @NotEmpty(message = "At least one service type is required")
    private Set<ServiceType> serviceTypes;
}
