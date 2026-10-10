package com.restaurant.saas_restaurant.menu.supplement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplementResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}