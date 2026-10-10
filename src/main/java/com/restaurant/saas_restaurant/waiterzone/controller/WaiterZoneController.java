package com.restaurant.saas_restaurant.waiterzone.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.waiterzone.dto.AssignWaiterRequest;
import com.restaurant.saas_restaurant.waiterzone.dto.WaiterZoneResponse;
import com.restaurant.saas_restaurant.waiterzone.service.WaiterZoneService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/zones")
@RequiredArgsConstructor
public class WaiterZoneController {
    private final WaiterZoneService waiterZoneService;

    @PutMapping("/{zoneId}/waiter")
    public ResponseEntity<ApiResponse<WaiterZoneResponse>> assignWaiter(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @Valid @RequestBody AssignWaiterRequest request,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        WaiterZoneResponse response = waiterZoneService.assign(restaurantId, zoneId, request, actingUserId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success(200, "Waiter assigned successfully", response));
    }
    @GetMapping("/waiter-assignments")
    public ResponseEntity<ApiResponse<List<WaiterZoneResponse>>> getAllAssignments(
            @PathVariable Long restaurantId, @RequestHeader(value = "X-User-Id") Long actingUserId) {
        List<WaiterZoneResponse> response = waiterZoneService.getAllByRestaurant(restaurantId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Waiter assignments retrieved successfully", response));
    }
    @GetMapping("/{zoneId}/waiter")
    public ResponseEntity<ApiResponse<WaiterZoneResponse>> getResponsibleWaiter(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @RequestHeader(value = "X-User-Id") Long actingUserId) {
        WaiterZoneResponse response = waiterZoneService.getByZone(restaurantId, zoneId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Responsible waiter retrieved successfully", response));
    }
    @DeleteMapping("/{zoneId}/waiter")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @RequestHeader(value = "X-User-Id") Long actingUserId) {
        waiterZoneService.remove(restaurantId, zoneId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Waiter unassigned successfully", null));
    }
}