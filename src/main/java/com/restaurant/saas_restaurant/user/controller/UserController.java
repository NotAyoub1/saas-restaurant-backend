package com.restaurant.saas_restaurant.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.user.dto.CreateUserRequest;
import com.restaurant.saas_restaurant.user.dto.UpdateUserRequest;
import com.restaurant.saas_restaurant.user.dto.UpdateUserRolesRequest;
import com.restaurant.saas_restaurant.user.dto.UserResponse;
import com.restaurant.saas_restaurant.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "User created successfully", response));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id, @RequestHeader("X-User-Id") Long actingUserId) {
        UserResponse response = userService.getById(id, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "User retrieved successfully", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> search(@RequestParam(required = false) String email,@RequestParam(value = "role",required = false) RoleName role,@RequestParam(value = "page",defaultValue = "0") int page,@RequestParam(value = "size",defaultValue = "20") int size,@RequestHeader("X-User-Id") Long actingUserId) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<UserResponse> response = userService.search(email, role, actingUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Users retrieved successfully", response));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request, @RequestHeader("X-User-Id") Long actingUserId) {
        UserResponse response = userService.update(id, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "User updated successfully", response));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request, @RequestHeader("X-User-Id") Long actingUserId) {
        UserResponse response = userService.updateRoles(id, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "User roles updated successfully", response));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long id, @RequestHeader("X-User-Id") Long actingUserId) {
        userService.softDelete(id, "acting-user-" + actingUserId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "User deleted successfully", null));
    }
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable Long id, @RequestHeader("X-User-Id") Long actingUserId) {
        userService.restore(id, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "User restored successfully", null));
    }
}
