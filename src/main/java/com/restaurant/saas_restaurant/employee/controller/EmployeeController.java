package com.restaurant.saas_restaurant.employee.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.employee.dto.AssignEmployeeRequest;
import com.restaurant.saas_restaurant.employee.dto.EmployeeResponse;
import com.restaurant.saas_restaurant.employee.dto.UpdateEmployeeRequest;
import com.restaurant.saas_restaurant.employee.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @PathVariable Long restaurantId, @Valid @RequestBody AssignEmployeeRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        EmployeeResponse response = employeeService.create(restaurantId, request, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Employee created successfully", response));
    }
    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getById(
            @PathVariable Long restaurantId, @PathVariable Long employeeId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        EmployeeResponse response = employeeService.getById(restaurantId, employeeId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Employee retrieved successfully", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> search(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) RoleName role,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<EmployeeResponse> response = employeeService.search(restaurantId, email, firstName, lastName, role, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Employees retrieved successfully", response));
    }
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> searchDeleted(
            @PathVariable Long restaurantId,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<EmployeeResponse> response = employeeService.searchDeleted(restaurantId, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Deleted employees retrieved successfully", response));
    }
    @PutMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long restaurantId, @PathVariable Long employeeId, @Valid @RequestBody UpdateEmployeeRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        EmployeeResponse response = employeeService.update(employeeId, restaurantId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Employee updated successfully", response));
    }
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long restaurantId, @PathVariable Long employeeId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        employeeService.softDelete(employeeId, restaurantId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Employee deleted successfully", null));
    }
    @PostMapping("/{employeeId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable Long restaurantId, @PathVariable Long employeeId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        employeeService.restore(employeeId, restaurantId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Employee restored successfully", null));
    }
}