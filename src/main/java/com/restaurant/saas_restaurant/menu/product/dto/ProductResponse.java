package com.restaurant.saas_restaurant.menu.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private Long categoryId;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}