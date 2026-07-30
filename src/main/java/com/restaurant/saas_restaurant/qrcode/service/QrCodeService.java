package com.restaurant.saas_restaurant.qrcode.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.restaurant.saas_restaurant.qrcode.dto.QrScanResponse;
import com.restaurant.saas_restaurant.table.dto.RestaurantTableResponse;
import com.restaurant.saas_restaurant.table.entity.RestaurantTable;
import com.restaurant.saas_restaurant.table.repository.RestaurantTableRepository;
import com.restaurant.saas_restaurant.table.service.RestaurantTableService;
import com.restaurant.saas_restaurant.waiterzone.entity.WaiterZone;
import com.restaurant.saas_restaurant.waiterzone.repository.WaiterZoneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final RestaurantTableService restaurantTableService;
    private final RestaurantTableRepository restaurantTableRepository;
    private final WaiterZoneRepository waiterZoneRepository;

    public byte[] generateTableQrCode(Long restaurantId,Long zoneId,Long tableId) {
        RestaurantTableResponse restaurantTable =
            restaurantTableService.getById(restaurantId, zoneId, tableId);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(restaurantTable.getQrCodeToken(),BarcodeFormat.QR_CODE,300,300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix,"PNG",outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Unable to generate QR Code", exception);
        }
    }
    @Transactional(readOnly = true)
    public QrScanResponse scan(String qrCodeToken) {
        RestaurantTable restaurantTable = restaurantTableRepository.findByQrCodeToken(qrCodeToken)
            .orElseThrow(() -> new NoSuchElementException("Qr Code not found!"));
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(restaurantTable.getZone().getId()).orElse(null);
        return new QrScanResponse(
            restaurantTable.getZone().getRestaurant().getId(),
            restaurantTable.getZone().getRestaurant().getName(),
            restaurantTable.getZone().getId(),
            restaurantTable.getZone().getName(),
            restaurantTable.getId(),
            restaurantTable.getTableNumber(),
            restaurantTable.getCapacity(),
            waiterZone != null ? waiterZone.getWaiter().getId() : null,
            waiterZone != null ? waiterZone.getWaiter().getFirstName() : null,
            waiterZone != null ? waiterZone.getWaiter().getLastName() : null
        );
    }
}