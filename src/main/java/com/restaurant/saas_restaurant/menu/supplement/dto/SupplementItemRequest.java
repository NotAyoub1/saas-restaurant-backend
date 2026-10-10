package com.restaurant.saas_restaurant.menu.supplement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplementItemRequest {
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal price;
}