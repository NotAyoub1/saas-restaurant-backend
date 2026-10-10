package com.restaurant.saas_restaurant.menu.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuResponse {
    private Long restaurantId;
    private List<MenuCategoryResponse> categories;
}