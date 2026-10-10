package com.restaurant.saas_restaurant.order.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderSupplementResponse {
    private String name;
    private BigDecimal price;
}