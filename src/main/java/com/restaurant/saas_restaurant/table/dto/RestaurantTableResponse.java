package com.restaurant.saas_restaurant.table.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantTableResponse {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String qrCodeToken;
    private Long zoneId;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
