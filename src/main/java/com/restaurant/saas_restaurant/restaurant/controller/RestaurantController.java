package com.restaurant.saas_restaurant.restaurant.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.restaurant.dto.CreateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.dto.RestaurantResponse;
import com.restaurant.saas_restaurant.restaurant.dto.UpdateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.service.RestaurantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @Valid @RequestBody CreateRestaurantRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        RestaurantResponse response = restaurantService.create(request, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Restaurant created successfully", response));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getById(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Restaurant retrieved successfully", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<RestaurantResponse> response = restaurantService.search(name, email, ownerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Restaurants retrieved successfully", response));
    }
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> searchDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<RestaurantResponse> response = restaurantService.searchDeleted(actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Deleted restaurants retrieved successfully", response));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateRestaurantRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        RestaurantResponse response = restaurantService.update(id, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Restaurant updated successfully", response));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long id, @RequestHeader("X-User-Id") Long actingUserId) {
        restaurantService.softDelete(id, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Restaurant deleted successfully", null));
    }
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable Long id, @RequestHeader("X-User-Id") Long actingUserId) {
        restaurantService.restore(id, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Restaurant restored successfully", null));
    }
}