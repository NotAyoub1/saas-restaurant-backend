package com.restaurant.saas_restaurant.table.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.saas_restaurant.qrcode.service.QrCodeService;
import com.restaurant.saas_restaurant.table.dto.CreateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.dto.RestaurantTableResponse;
import com.restaurant.saas_restaurant.table.dto.UpdateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.service.RestaurantTableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}")
@RequiredArgsConstructor
public class RestaurantTableController {
    private final RestaurantTableService restaurantTableService;
    private final QrCodeService qrCodeService;

    @PostMapping("/zones/{zoneId}/tables")
    public ResponseEntity<RestaurantTableResponse> create(@Valid @RequestBody CreateRestaurantTableRequest request, @PathVariable Long zoneId,@PathVariable Long restaurantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantTableService.create(request, zoneId, restaurantId));
    }
    @GetMapping("/tables")
    public ResponseEntity<List<RestaurantTableResponse>> getAll(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantTableService.getAllByRestaurant(restaurantId));
    }
    @GetMapping("/zones/{zoneId}/tables")
    public ResponseEntity<List<RestaurantTableResponse>> getAllByZone(@PathVariable Long restaurantId, @PathVariable Long zoneId) {
        return ResponseEntity.ok(restaurantTableService.getAllByZone(restaurantId, zoneId));
    }
    @GetMapping("/zones/{zoneId}/tables/{tableId}")
    public ResponseEntity<RestaurantTableResponse> getByTableIdAndZoneIdAndRestaurantId(@PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId) {
        return ResponseEntity.ok(restaurantTableService.getById(restaurantId, zoneId, tableId));
    }
    @PutMapping("/zones/{zoneId}/tables/{tableId}")
    public ResponseEntity<RestaurantTableResponse> update(@PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId, @Valid @RequestBody UpdateRestaurantTableRequest request) {
        return ResponseEntity.ok(restaurantTableService.update(request, zoneId, restaurantId, tableId));
    }
    @DeleteMapping("/zones/{zoneId}/tables/{tableId}")
    public ResponseEntity<Void> delete(@PathVariable Long restaurantId, @PathVariable Long zoneId, @PathVariable Long tableId) {
        restaurantTableService.delete(tableId, zoneId, restaurantId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping(value = "/zones/{zoneId}/tables/{tableId}/qr-code", produces = "image/png")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long restaurantId,@PathVariable Long zoneId,@PathVariable Long tableId) {
        return ResponseEntity.ok(qrCodeService.generateTableQrCode(restaurantId, zoneId, tableId));
    }
}
