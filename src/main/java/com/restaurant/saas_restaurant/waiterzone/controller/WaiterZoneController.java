package com.restaurant.saas_restaurant.waiterzone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.saas_restaurant.waiterzone.dto.AssignWaiterRequest;
import com.restaurant.saas_restaurant.waiterzone.dto.WaiterZoneResponse;
import com.restaurant.saas_restaurant.waiterzone.service.WaiterZoneService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/zones")
@RequiredArgsConstructor
public class WaiterZoneController {
    private final WaiterZoneService waiterZoneService;

    @PutMapping("/{zoneId}/waiter")
    public ResponseEntity<WaiterZoneResponse> assignewaiter(@PathVariable Long restaurantId, @PathVariable Long zoneId, @Valid @RequestBody AssignWaiterRequest request) {
        return ResponseEntity.ok(waiterZoneService.assign(restaurantId, zoneId, request));
    }
    @GetMapping("/waiter-assignments")
    public ResponseEntity<List<WaiterZoneResponse>> getAllAssignments(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(waiterZoneService.getAllByRestaurant(restaurantId));
    }
    @GetMapping("/{zoneId}/waiter")
    public ResponseEntity<WaiterZoneResponse> getResponsibleWaiter(@PathVariable Long restaurantId, @PathVariable Long zoneId) {
        return ResponseEntity.ok(waiterZoneService.getByZone(restaurantId, zoneId));
    }
    @DeleteMapping("/{zoneId}/waiter")
    public ResponseEntity<Void> remove(@PathVariable Long restaurantId, @PathVariable Long zoneId) {
        waiterZoneService.remove(restaurantId,zoneId);
        return ResponseEntity.noContent().build();
    }
}
