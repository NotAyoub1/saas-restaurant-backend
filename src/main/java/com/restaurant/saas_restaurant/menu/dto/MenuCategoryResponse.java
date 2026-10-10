package com.restaurant.saas_restaurant.menu.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private List<MenuProductResponse> products;
}