package com.restaurant.saas_restaurant.menu.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.menu.product.dto.CreateProductRequest;
import com.restaurant.saas_restaurant.menu.product.dto.ProductResponse;
import com.restaurant.saas_restaurant.menu.product.dto.UpdateProductRequest;
import com.restaurant.saas_restaurant.menu.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @PathVariable Long restaurantId,
            @RequestPart("product") @Valid CreateProductRequest request,
            @RequestPart("image") MultipartFile image,
            @RequestHeader("X-User-Id") Long actingUserId) {
        ProductResponse response = productService.create(restaurantId, request, image, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Product created successfully", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> listByCategory(
            @PathVariable Long restaurantId,
            @RequestParam Long categoryId) {
        List<ProductResponse> response = productService.listByCategory(restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(200, "Products retrieved successfully", response));
    }
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long restaurantId, @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        ProductResponse response = productService.update(restaurantId, productId, request, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Product updated successfully", response));
    }
    @PatchMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateImage(
            @PathVariable Long restaurantId, @PathVariable Long productId,
            @RequestPart("image") MultipartFile image,
            @RequestHeader("X-User-Id") Long actingUserId) {
        ProductResponse response = productService.updateImage(restaurantId, productId, image, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Product image updated successfully", response));
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long restaurantId, @PathVariable Long productId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        productService.softDelete(restaurantId, productId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Product deleted successfully", null));
    }
    @PostMapping("/{productId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable Long restaurantId, @PathVariable Long productId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        productService.restore(restaurantId, productId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Product restored successfully", null));
    }
}