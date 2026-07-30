package com.restaurant.saas_restaurant.employee.controller;

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

import com.restaurant.saas_restaurant.employee.dto.CreateEmployeeRequest;
import com.restaurant.saas_restaurant.employee.dto.EmployeeResponse;
import com.restaurant.saas_restaurant.employee.dto.UpdateEmployeeRequest;
import com.restaurant.saas_restaurant.employee.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@PathVariable Long restaurantId, @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.create(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long restaurantId, @PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getById(restaurantId, employeeId));
    }
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAll(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(employeeService.findAllByRestaurantId(restaurantId));
    }
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long restaurantId, @PathVariable Long employeeId, @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(employeeId, restaurantId, request));
    }
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> delete(@PathVariable Long restaurantId, @PathVariable Long employeeId) {
        employeeService.delete(employeeId, restaurantId);
        return ResponseEntity.noContent().build(); //soft delete
    }
}
