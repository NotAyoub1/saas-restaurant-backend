package com.restaurant.saas_restaurant.menu.category.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    Long id;
    String name;
    String description;
    Long restaurantId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
