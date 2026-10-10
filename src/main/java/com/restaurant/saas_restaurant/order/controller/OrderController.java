package com.restaurant.saas_restaurant.order.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.order.dto.CreateOrderRequest;
import com.restaurant.saas_restaurant.order.dto.OrderResponse;
import com.restaurant.saas_restaurant.order.dto.OrderSummaryResponse;
import com.restaurant.saas_restaurant.order.dto.UpdateOrderRequest;
import com.restaurant.saas_restaurant.order.dto.UpdateOrderStatusRequest;
import com.restaurant.saas_restaurant.order.entity.OrderStatus;
import com.restaurant.saas_restaurant.order.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Pageable pageable(int page, int size) {
        return PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @PathVariable Long restaurantId, @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        OrderResponse response = orderService.create(restaurantId, request, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Order created successfully", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        OrderResponse response = orderService.getById(restaurantId, orderId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Order retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> search(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long actingUserId) {
        Page<OrderSummaryResponse> response = orderService.search(
            restaurantId, zoneId, tableId, status, from, to, actingUserId, pageable(page, size));
        return ResponseEntity.ok(ApiResponse.success(200, "Orders retrieved successfully", response));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> searchMine(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long actingUserId) {
        Page<OrderSummaryResponse> response = orderService.searchMine(
            restaurantId, status, actingUserId, pageable(page, size));
        return ResponseEntity.ok(ApiResponse.success(200, "Your orders retrieved successfully", response));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        OrderResponse response = orderService.update(restaurantId, orderId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Order updated successfully", response));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        OrderResponse response = orderService.updateStatus(restaurantId, orderId, request.getStatus(), actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Order status updated successfully", response));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        OrderResponse response = orderService.cancel(restaurantId, orderId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Order cancelled successfully", response));
    }
}