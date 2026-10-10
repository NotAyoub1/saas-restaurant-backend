package com.restaurant.saas_restaurant.menu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.menu.dto.MenuResponse;
import com.restaurant.saas_restaurant.menu.service.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<MenuResponse>> getMenu(@PathVariable Long restaurantId) {
        MenuResponse response = menuService.getMenu(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(200, "Menu retrieved successfully", response));
    }
}