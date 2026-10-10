package com.restaurant.saas_restaurant.menu.variant.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariantItemRequest {
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal priceModifier;
}