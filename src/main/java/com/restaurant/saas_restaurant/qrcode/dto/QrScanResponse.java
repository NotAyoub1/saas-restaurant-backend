package com.restaurant.saas_restaurant.qrcode.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class QrScanResponse {
    private Long restaurantId;
    private String restaurantName;
    private Long zoneId;
    private String zoneName;
    private Long tableId;
    private String tableNumber;
    private Integer tablecapacity;
    private Long waiterId;
    private String waiterFirstName;
    private String waiterLastName;
}
