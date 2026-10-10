package com.restaurant.saas_restaurant.menu.variant.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VariantResponse {
    private Long id;
    private String name;
    private BigDecimal priceModifier;
}