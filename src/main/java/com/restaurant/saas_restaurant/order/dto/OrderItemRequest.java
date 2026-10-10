package com.restaurant.saas_restaurant.order.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull
    private Long productId;

    private Long variantId; // optional

    @NotNull
    @Min(1)
    @Max(99)
    private Integer quantity;

    private List<Long> supplementIds; // optional
}