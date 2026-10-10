package com.restaurant.saas_restaurant.payment.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.saas_restaurant.common.dto.ApiResponse;
import com.restaurant.saas_restaurant.payment.dto.CreatePaymentRequest;
import com.restaurant.saas_restaurant.payment.dto.InvoiceFile;
import com.restaurant.saas_restaurant.payment.dto.PaymentResponse;
import com.restaurant.saas_restaurant.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/orders/{orderId}")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("X-User-Id") Long actingUserId) {
        PaymentResponse response = paymentService.pay(restaurantId, orderId, request, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "Payment recorded successfully", response));
    }

    @GetMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        PaymentResponse response = paymentService.getByOrder(restaurantId, orderId, actingUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "Payment retrieved successfully", response));
    }

    @GetMapping("/invoice")
    public ResponseEntity<byte[]> getInvoice(
            @PathVariable Long restaurantId, @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long actingUserId) {
        InvoiceFile file = paymentService.getInvoice(restaurantId, orderId, actingUserId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(file.getContent());
    }
}