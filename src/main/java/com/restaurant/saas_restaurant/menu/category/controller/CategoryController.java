package com.restaurant.saas_restaurant.menu.category.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.menu.category.dto.CategoryRequest;
import com.restaurant.saas_restaurant.menu.category.dto.CategoryResponse;
import com.restaurant.saas_restaurant.menu.category.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @PathVariable Long restaurantId, @Valid @RequestBody CategoryRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        CategoryResponse response = categoryService.create(restaurantId, request, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Category created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        List<CategoryResponse> response = categoryService.list(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(200, "Categories retrieved successfully", response));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long restaurantId, @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        CategoryResponse response = categoryService.update(restaurantId, categoryId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Category updated successfully", response));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long restaurantId, @PathVariable Long categoryId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        categoryService.softDelete(restaurantId, categoryId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Category deleted successfully", null));
    }

    @PostMapping("/{categoryId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable Long restaurantId, @PathVariable Long categoryId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        categoryService.restore(restaurantId, categoryId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Category restored successfully", null));
    }
}