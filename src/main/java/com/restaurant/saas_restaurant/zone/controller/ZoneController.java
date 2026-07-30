package com.restaurant.saas_restaurant.zone.controller;

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

import com.restaurant.saas_restaurant.zone.dto.CreateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.UpdateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.ZoneResponse;
import com.restaurant.saas_restaurant.zone.service.ZoneService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/zones")
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneService zoneService;

    @PostMapping
    public ResponseEntity<ZoneResponse> create(@PathVariable Long restaurantId, @Valid @RequestBody CreateZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zoneService.create(restaurantId, request));
    }
    @GetMapping
    public ResponseEntity<List<ZoneResponse>> getAll(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(zoneService.getAllByRestaurantID(restaurantId));
    }
    @GetMapping("/{zoneId}")
    public ResponseEntity<ZoneResponse> getByZoneIdRestaurantId(@PathVariable Long restaurantId, @PathVariable Long zoneId) {
        return ResponseEntity.ok(zoneService.getById(restaurantId, zoneId));
    }
    @PutMapping("/{zoneId}")
    public ResponseEntity<ZoneResponse> update(@PathVariable Long restaurantId, @PathVariable Long zoneId, @Valid @RequestBody UpdateZoneRequest request) {
        return ResponseEntity.ok(zoneService.update(zoneId, restaurantId, request));
    }
    @DeleteMapping("/{zoneId}")
    public ResponseEntity<Void> delete(@PathVariable Long restaurantId, @PathVariable Long zoneId) {
        zoneService.delete(restaurantId, zoneId);
        return ResponseEntity.noContent().build();
    }
}
