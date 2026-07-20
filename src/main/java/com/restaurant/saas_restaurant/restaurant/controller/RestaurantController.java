package com.restaurant.saas_restaurant.restaurant.controller;

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

import com.restaurant.saas_restaurant.restaurant.dto.CreateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.dto.RestaurantResponse;
import com.restaurant.saas_restaurant.restaurant.dto.UpdateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.service.RestaurantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse respons = restaurantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respons);
    }
    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAll() {
        return ResponseEntity.ok(restaurantService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id,@Valid @RequestBody UpdateRestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.update(id,request));
    }
    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        restaurantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
