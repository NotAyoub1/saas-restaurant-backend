package com.restaurant.saas_restaurant.qrcode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.saas_restaurant.qrcode.dto.QrScanResponse;
import com.restaurant.saas_restaurant.qrcode.service.QrCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/qr")
@RequiredArgsConstructor
public class PublicQrController {
    private final QrCodeService qrCodeService;

    @GetMapping("/{qrCodeToken}")
    public ResponseEntity<QrScanResponse> scan(@PathVariable String qrCodeToken) {
        return ResponseEntity.ok(qrCodeService.scan(qrCodeToken));
    }
}