package com.restaurant.saas_restaurant.zone.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.zone.dto.CreateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.UpdateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.ZoneResponse;
import com.restaurant.saas_restaurant.zone.service.ZoneService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/zones")
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneService zoneService;

    @PostMapping
    public ResponseEntity<ApiResponse<ZoneResponse>> create(
            @PathVariable Long restaurantId, @Valid @RequestBody CreateZoneRequest request,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        ZoneResponse response = zoneService.create(restaurantId, request, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Zone created successfully", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ZoneResponse>>> search(
            @PathVariable Long restaurantId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ZoneResponse> response = zoneService.search(restaurantId, name, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Zones retrieved successfully", response));
    }
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<Page<ZoneResponse>>> searchDeleted(
            @PathVariable Long restaurantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ZoneResponse> response = zoneService.searchDeleted(restaurantId, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Deleted zones retrieved successfully", response));
    }
    @GetMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<ZoneResponse>> getById(
            @PathVariable Long restaurantId, @PathVariable Long zoneId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        ZoneResponse response = zoneService.getById(restaurantId, zoneId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Zone retrieved successfully", response));
    }
    @PutMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<ZoneResponse>> update(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @Valid @RequestBody UpdateZoneRequest request,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        ZoneResponse response = zoneService.update(zoneId, restaurantId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Zone updated successfully", response));
    }
    @DeleteMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long restaurantId, @PathVariable Long zoneId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        zoneService.softDelete(restaurantId, zoneId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Zone deleted successfully", null));
    }
    @PostMapping("/{zoneId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable Long restaurantId, @PathVariable Long zoneId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        zoneService.restore(restaurantId, zoneId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Zone restored successfully", null));
    }
}