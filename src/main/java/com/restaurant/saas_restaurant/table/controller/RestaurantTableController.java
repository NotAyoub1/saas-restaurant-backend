package com.restaurant.saas_restaurant.table.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.qrcode.service.QrCodeService;
import com.restaurant.saas_restaurant.table.dto.CreateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.dto.RestaurantTableResponse;
import com.restaurant.saas_restaurant.table.dto.UpdateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.service.RestaurantTableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}")
@RequiredArgsConstructor
public class RestaurantTableController {
    private final RestaurantTableService restaurantTableService;
    private final QrCodeService qrCodeService;

    @PostMapping("/zones/{zoneId}/tables")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> create(
            @Valid @RequestBody CreateRestaurantTableRequest request,
            @PathVariable Long zoneId, @PathVariable Long restaurantId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        RestaurantTableResponse response = restaurantTableService.create(request, zoneId, restaurantId, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Table created successfully", response));
    }
    @GetMapping("/tables")
    public ResponseEntity<ApiResponse<Page<RestaurantTableResponse>>> getAll(
            @PathVariable Long restaurantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<RestaurantTableResponse> response = restaurantTableService.searchByRestaurant(restaurantId, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Tables retrieved successfully", response));
    }
    @GetMapping("/zones/{zoneId}/tables")
    public ResponseEntity<ApiResponse<Page<RestaurantTableResponse>>> getAllByZone(
            @PathVariable Long restaurantId, @PathVariable Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<RestaurantTableResponse> response = restaurantTableService.searchByZone(restaurantId, zoneId, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Tables retrieved successfully", response));
    }
    @GetMapping("/zones/{zoneId}/tables/deleted")
    public ResponseEntity<ApiResponse<Page<RestaurantTableResponse>>> searchDeleted(
            @PathVariable Long restaurantId, @PathVariable Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<RestaurantTableResponse> response = restaurantTableService.searchDeleted(restaurantId, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Deleted tables retrieved successfully", response));
    }
    @GetMapping("/zones/{zoneId}/tables/{tableId}")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> getByTableIdAndZoneIdAndRestaurantId(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        RestaurantTableResponse response = restaurantTableService.getById(restaurantId, zoneId, tableId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Table retrieved successfully", response));
    }
    @PutMapping("/zones/{zoneId}/tables/{tableId}")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> update(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId,
            @Valid @RequestBody UpdateRestaurantTableRequest request,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        RestaurantTableResponse response = restaurantTableService.update(request, zoneId, restaurantId, tableId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Table updated successfully", response));
    }
    @DeleteMapping("/zones/{zoneId}/tables/{tableId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        restaurantTableService.softDelete(tableId, zoneId, restaurantId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Table deleted successfully", null));
    }
    @PostMapping("/zones/{zoneId}/tables/{tableId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId,
            @RequestHeader(value = "X-User-Id") Long actingUserId) {
        restaurantTableService.restore(tableId, zoneId, restaurantId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Table restored successfully", null));
    }
    @GetMapping(value = "/zones/{zoneId}/tables/{tableId}/qr-code", produces = "image/png")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId, @RequestHeader(value = "X-User-Id") Long actingUserId) {
        return ResponseEntity.ok(qrCodeService.generateTableQrCode(restaurantId, zoneId, tableId, actingUserId));
    }
}