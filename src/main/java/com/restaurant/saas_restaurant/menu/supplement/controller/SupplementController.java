package com.restaurant.saas_restaurant.menu.supplement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.menu.supplement.dto.ReplaceSupplementsRequest;
import com.restaurant.saas_restaurant.menu.supplement.dto.SupplementResponse;
import com.restaurant.saas_restaurant.menu.supplement.service.SupplementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/products/{productId}/supplements")
@RequiredArgsConstructor
public class SupplementController {
    private final SupplementService supplementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplementResponse>>> list(
            @PathVariable Long restaurantId, @PathVariable Long productId) {
        List<SupplementResponse> response = supplementService.list(restaurantId, productId);
        return ResponseEntity.ok(ApiResponse.success(200, "Supplements retrieved successfully", response));
    }
    @PutMapping
    public ResponseEntity<ApiResponse<List<SupplementResponse>>> replaceAll(
            @PathVariable Long restaurantId, @PathVariable Long productId,
            @Valid @RequestBody ReplaceSupplementsRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        List<SupplementResponse> response = supplementService.replaceAll(restaurantId, productId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Supplements updated successfully", response));
    }
}