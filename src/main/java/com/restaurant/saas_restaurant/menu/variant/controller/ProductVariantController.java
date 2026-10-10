package com.restaurant.saas_restaurant.menu.variant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.menu.variant.dto.ReplaceVariantsRequest;
import com.restaurant.saas_restaurant.menu.variant.dto.VariantResponse;
import com.restaurant.saas_restaurant.menu.variant.service.ProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService variantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VariantResponse>>> list(
            @PathVariable Long restaurantId, @PathVariable Long productId) {
        List<VariantResponse> response = variantService.list(restaurantId, productId);
        return ResponseEntity.ok(ApiResponse.success(200, "Variants retrieved successfully", response));
    }
    @PutMapping
    public ResponseEntity<ApiResponse<List<VariantResponse>>> replaceAll(
            @PathVariable Long restaurantId, @PathVariable Long productId,
            @Valid @RequestBody ReplaceVariantsRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        List<VariantResponse> response = variantService.replaceAll(restaurantId, productId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Variants updated successfully", response));
    }
}