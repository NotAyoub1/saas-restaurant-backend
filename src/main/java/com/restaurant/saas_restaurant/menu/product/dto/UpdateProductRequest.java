package com.restaurant.saas_restaurant.menu.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    @NotNull
    private Long categoryId;
}