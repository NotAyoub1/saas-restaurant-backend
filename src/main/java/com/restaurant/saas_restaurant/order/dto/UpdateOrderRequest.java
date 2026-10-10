package com.restaurant.saas_restaurant.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderRequest {

    @Size(max = 500)
    private String notes; // null = keep the current notes

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}