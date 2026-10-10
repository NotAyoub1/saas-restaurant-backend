package com.restaurant.saas_restaurant.order.dto;

import com.restaurant.saas_restaurant.order.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
}